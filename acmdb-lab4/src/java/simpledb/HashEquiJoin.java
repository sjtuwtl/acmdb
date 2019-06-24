package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class HashEquiJoin extends Operator {

    private static final long serialVersionUID = 1L;
    private JoinPredicate joinPredicate;
    private DbIterator child1, child2;
    private HashMap<Field, ArrayList<Tuple> > table;

    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     *
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public HashEquiJoin(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        joinPredicate = p;
        this.child1 = child1;
        this.child2 = child2;
    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return joinPredicate;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return TupleDesc.merge(child1.getTupleDesc(), child2.getTupleDesc());
    }

    public String getJoinField1Name()
    {
        // some code goes here
	    return child1.getTupleDesc().getFieldName(joinPredicate.getField1());
    }

    public String getJoinField2Name()
    {
        // some code goes here
        return child2.getTupleDesc().getFieldName(joinPredicate.getField2());
    }


    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        super.open();
        child1.open();
        child2.open();
        table = new HashMap<Field, ArrayList<Tuple> >();
        while (child1.hasNext()) {
            Tuple tuple = child1.next();
            Field field = tuple.getField(joinPredicate.getField1());
            if (table.containsKey(field))
                table.get(field).add(tuple);
            else {
                ArrayList<Tuple> arrayList = new ArrayList<Tuple>();
                arrayList.add(tuple);
                table.put(field, arrayList);
            }
        }
        child1.rewind();
    }

    public void close() {
        // some code goes here
        super.close();
        child1.close();
        child2.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        child1.rewind();
        child2.rewind();
        table.clear();
        while (child1.hasNext()) {
            Tuple tuple = child1.next();
            Field field = tuple.getField(joinPredicate.getField1());
            if (table.containsKey(field))
                table.get(field).add(tuple);
            else {
                ArrayList<Tuple> arrayList = new ArrayList<Tuple>();
                arrayList.add(tuple);
                table.put(field, arrayList);
            }
        }
        child1.rewind();
    }

    transient Iterator<Tuple> listIt = null;

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, there will be two copies of the join attribute in
     * the results. (Removing such duplicate columns can be done with an
     * additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */

    private Tuple tmp;
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (tmp == null) {
            if (!child2.hasNext())
                return null;
            else {
                tmp = child2.next();
                Field field = tmp.getField(joinPredicate.getField2());
                while (!table.containsKey(field)){
                    if (!child2.hasNext())
                        return null;
                    tmp = child2.next();
                    field = tmp.getField(joinPredicate.getField2());
                }
                ArrayList<Tuple> arrayList = table.get(field);
                listIt = arrayList.iterator();
            }
        }
        if (listIt != null) {
            while (listIt.hasNext()) {
                Tuple tuple = listIt.next();
                if (!joinPredicate.filter(tuple, tmp))
                    continue;
                Tuple t = new Tuple(TupleDesc.merge(tuple.getTupleDesc(), tmp.getTupleDesc()));
                for (int i = 0; i < tuple.getTupleDesc().numFields(); i++)
                    t.setField(i, tuple.getField(i));
                for (int i = 0; i < tmp.getTupleDesc().numFields(); i++)
                    t.setField(i + tuple.getTupleDesc().numFields(), tmp.getField(i));
                return t;
            }
        }
        while (child2.hasNext()) {
            tmp = child2.next();
            Field field = tmp.getField(joinPredicate.getField2());
            if (!table.containsKey(field))
                continue;
            ArrayList<Tuple> arrayList = table.get(field);
            listIt = arrayList.iterator();
            while (listIt.hasNext()){
                Tuple tuple = listIt.next();
                if (!joinPredicate.filter(tuple, tmp))
                    continue;
                Tuple t = new Tuple(TupleDesc.merge(tuple.getTupleDesc(), tmp.getTupleDesc()));
                for (int i = 0; i < tuple.getTupleDesc().numFields(); i++)
                    t.setField(i, tuple.getField(i));
                for (int i = 0; i < tmp.getTupleDesc().numFields(); i++)
                    t.setField(i + tuple.getTupleDesc().numFields(), tmp.getField(i));
                return t;
            }
        }
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        DbIterator[] tmp = new DbIterator[]{child1, child2};
        return tmp;
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        this.child1 = children[0];
        this.child2 = children[1];
    }

}
