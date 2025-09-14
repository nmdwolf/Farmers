package core.contracts;

import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

import java.util.ArrayDeque;

public class Logger {

    private final ArrayDeque<Log> logs;
    private final GameObject object;

    public Logger(GameObject object) {
        this.object = object;
        logs = new ArrayDeque<>();
    }

    public void logLabour(String resource, boolean finished) {
        if(!(object instanceof Operational<?>))
            throw new IllegalStateException("The object used to initialize this Logger is not of type 'Operational'.");

        logs.addLast(new Log(Log.LABOUR,object.getClassLabel() + " extracted " + resource + (finished ? " and the source was emptied." : ".")));
    }

    public void logConstruction(boolean built) {
        if(!(object instanceof Operational<?>))
            throw new IllegalStateException("The object used to initialize this Logger is not of type 'Operational'.");

        logs.addLast(new Log(Log.CONSTRUCT, object.getClassLabel() + (built ? " finished a construction." : " progressed in a construction.")));
    }

    public void logAttack(boolean killed) {
        if(!(object instanceof Aggressive))
            throw new IllegalStateException("The object used to initialize this Logger is not of type 'Aggressive'.");

        logs.addLast(new Log(Log.ATTACK, object.getClassLabel() + " attacked his target" + (killed ? " and the enemy was killed." : ".")));
    }

    public String getTranscript() {
        StringBuilder transcript = new StringBuilder();
        while(!logs.isEmpty())
            transcript.append(logs.pop()).append("\n");
        return transcript.toString();
    }

    public Log pop() {
        return logs.pop();
    }

    public int size() {
        return logs.size();
    }

    // Not a record since this would make extension/modding impossible
    public static class Log {
        public final static int LABOUR = 0;
        public final static int CONSTRUCT = 1;
        public final static int ATTACK = 2;

        public final int type;
        public final String description;

        public Log(int type, String description) {
            this.type = type;
            this.description = description;
        }
    }
}
