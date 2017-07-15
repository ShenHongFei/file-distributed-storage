package system

import groovy.transform.ToString

@ToString(includeNames = true)
class FileInfo implements Serializable{
    UUID     uuid
    String   name
    Integer  size
    NodeInfo main
    NodeInfo backup
}