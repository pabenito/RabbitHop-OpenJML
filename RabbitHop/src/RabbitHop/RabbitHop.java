package RabbitHop;

import Maude.Module;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RabbitHop extends Module{
    public final static char RIGHT_RABBIT = 'x';
    public final static char LEFT_RABBIT = 'o';
    public final char SPACE = '_';
    private List<Rabbit> rabbitList;
    private Random random;

    // Constructors

    public RabbitHop(int rabbits){
        super();
        placeRabbits(rabbits);
    }

    public RabbitHop(String rabbits){
        super();
        placeRabbits(rabbits);
    }

    private void placeRabbits(int rabbits) {
        rabbitList = new ArrayList<>(rabbits * 2 + 1);
        for(int i = 0; i < rabbits; i++){
            rabbitList.add(Rabbit.RIGHT);
        }
        rabbitList.add(Rabbit.SPACE);
        for(int i = rabbits + 1; i <= rabbits * 2; i++){
            rabbitList.add(Rabbit.LEFT);
        }
    }

    private void placeRabbits(String rabbits) {
        rabbitList = new ArrayList<>(rabbits.length());
        for(char rabbit : rabbits.toCharArray())
            rabbitList.add(charToRabbit(rabbit));
    }

    // Query

    private void checkRabbitInList(int rabbit) {
        if(rabbit < 0 || rabbit >= rabbitList.size())
            throw new ArrayIndexOutOfBoundsException(String.format("The rabbit %d is not in the list of size %d", rabbit, rabbitList.size()));
    }

    public boolean canAdvance(int rabbit){
        checkRabbitInList(rabbit);
        switch (rabbitList.get(rabbit)){
            case RIGHT:
                if (rabbit == rabbitList.size() - 1)
                    return false;
                return rabbitList.get(rabbit + 1) == Rabbit.SPACE;
            case LEFT:
                if (rabbit == 0)
                    return false;
                return rabbitList.get(rabbit - 1) == Rabbit.SPACE;
            case SPACE: return false;
            default: throw new NullPointerException(String.format("Unexpected null rabbit at %d in %s", rabbit, this));
        }
    }

    public boolean canHop(int rabbit){
        checkRabbitInList(rabbit);
        switch (rabbitList.get(rabbit)){
            case RIGHT:
                if (rabbit >= rabbitList.size() - 2)
                    return false;
                return rabbitList.get(rabbit + 1) != Rabbit.SPACE && rabbitList.get(rabbit + 2) == Rabbit.SPACE;
            case LEFT:
                if (rabbit <= 1)
                    return false;
                return rabbitList.get(rabbit - 1) != Rabbit.SPACE && rabbitList.get(rabbit - 2) == Rabbit.SPACE;
            case SPACE: return false;
            default: throw new NullPointerException(String.format("Unexpected null rabbit at %d in %s", rabbit, this));
        }
    }

    public boolean canMove(int rabbit){
        return canAdvance(rabbit) || canHop(rabbit);
    }

    // Action

    public void advance(int rabbit){
        if(!canAdvance(rabbit))
            throw new RuntimeException(String.format("Rabbit %d cant advance in %s", rabbit, this));
        String preState = this.toString();
        Integer to = null;
        switch (rabbitList.get(rabbit)){
            case RIGHT:
                to = rabbit + 1;
                break;
            case LEFT:
                to = rabbit - 1;
                break;
        }
        swap(rabbit, to);
        addTrace("advance", rabbitList.get(to), rabbit, to, preState);
    }

    public void hop(int rabbit){
        if(!canHop(rabbit))
            throw new RuntimeException(String.format("Rabbit %d cant hop in %s", rabbit, this));
        String preState = this.toString();
        Integer to = null;
        switch (rabbitList.get(rabbit)){
            case RIGHT:
                to = rabbit + 2;
                break;
            case LEFT:
                to = rabbit - 2;
                break;
        }
        swap(rabbit, to);
        addTrace("hop", rabbitList.get(to), rabbit, to, preState);
    }

    public void move(Integer rabbit) {
        if(canAdvance(rabbit))
            advance(rabbit);
        else if (canHop(rabbit))
            hop(rabbit);
        else
            new RuntimeException(String.format("Rabbit %d cant move in %s", rabbit, this));
    }

    // Auxiliary

    private void swap(int a, int b){
        Rabbit rabbitA = rabbitList.get(a);
        rabbitList.set(a, rabbitList.get(b));
        rabbitList.set(b, rabbitA);
    }

    private Rabbit charToRabbit(char rabbit) {
        switch (rabbit){
            case RIGHT_RABBIT: return Rabbit.RIGHT;
            case LEFT_RABBIT: return Rabbit.LEFT;
            case SPACE: return Rabbit.SPACE;
            default: throw new InvalidParameterException(String.format("Unexpected rabbit representation: %s", rabbit));
        }
    }

    private void addTrace(String oper, Rabbit rabbit, int from, int to, String preState) {
        addTrace(String.format("%s rabbit at %d %s to %d:\n\t%s ---[%s]---> %s",
                rabbit.toString(), from, oper, to, preState, oper, this));
    }

    // Print

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Rabbit rabbit : rabbitList){
            stringBuilder.append(print(rabbit));
        }
        return stringBuilder.toString();
    }

    private char print(Rabbit rabbit){
        switch (rabbit){
            case RIGHT: return RIGHT_RABBIT;
            case LEFT: return LEFT_RABBIT;
            case SPACE: return SPACE;
            default: throw new NullPointerException(String.format("Unexpected null rabbit at %d in %s", rabbit, this));
        }
    }

    // Maude

    @Override
    public void rewrite() {
        Integer rabbit;
        while ((rabbit = anyWhoCanMove()) != null)
            move(rabbit);
    }

    @Override
    public void fairRewrite(Random random) {
        this.random = random;
        Integer rabbit;
        while ((rabbit = anyWhoCanMoveRandom()) != null)
            move(rabbit);
    }

    @Override
    public RabbitHop clone(){
        return new RabbitHop(new ArrayList<>(rabbitList), getTrace());
    }

    private Integer anyWhoCanMove() {
        for (int i = 0; i < rabbitList.size(); i++)
            if(canMove(i))
                return i;
        return null;
    }

    private Integer anyWhoCanMoveRandom() {
        List<Integer> disorderedIndexes = randomIndexes(rabbitList.size());
        for (int i = 0; i < rabbitList.size(); i++)
            if(canMove(disorderedIndexes.get(i)))
                return disorderedIndexes.get(i);
        return null;
    }

    private List<Integer> randomIndexes(int size) {
        List<Integer> indexList = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            indexList.add(i);
        }
        for(int i = 0; i < size; i++){
            swap(random.nextInt(size), random.nextInt(size), indexList);
        }
        return indexList;
    }

    private void swap(int a, int b, List list){
        if(a < 0 || a >= list.size())
            throw new ArrayIndexOutOfBoundsException(String.format("Index a:%d is out of bounds for list of size %d", a, list.size()));
        if(b < 0 || b >= list.size())
            throw new ArrayIndexOutOfBoundsException(String.format("Index a:%d is out of bounds for list of size %d", a, list.size()));
        Object o = list.get(a);
        list.set(a, list.get(b));
        list.set(b, o);
    }

    // Node

    private RabbitHop(List<Rabbit> rabbitList, List<String> trace){
        super(trace);
        this.rabbitList = rabbitList;
    }

    @Override
    public List<Module> next() {
        List<Module> childs = new ArrayList<>();
        for (int i = 0; i < rabbitList.size(); i++){
            if(canAdvance(i)){
                RabbitHop clone = clone();
                clone.advance(i);
                childs.add(clone);
            }
            if(canHop(i)){
                RabbitHop clone = clone();
                clone.hop(i);
                childs.add(clone);
            }
        }
        return childs;
    }
}
