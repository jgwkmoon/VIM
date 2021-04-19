package pnu.stemlab.vimnavi;

public class VIMShortInst {
    public enum InstType {
        REROUTING("REROUTING"),
        ARRIVAL("ARRIVAL"), TURNING("TURNING"), STARTING("STARTING"), ENDING("ENDING"),
        SAFETY("SAFETY")
        ;

        private String name;
        private InstType(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }
    public final InstType type;
    public final String wayptId;
    public final String message;
    public VIMShortInst(InstType type, String wayptId, String message) {
        this.type = type;
        this.wayptId = wayptId;
        this.message = message;
    }
    public String getHeader() {
        return "[" + type + " on " + wayptId + "]";
    }
    public String getLongMessage() {
        return getHeader() + " " + message;
    }
    public String getShortMessage() {
        return message;
    }
    public String toString() {
        return getShortMessage();
    }
}
