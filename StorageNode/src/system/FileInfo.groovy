package system

class FileInfo implements Serializable{
    UUID     uuid
    String   name
    Integer  size
    NodeInfo main
    NodeInfo backup
}