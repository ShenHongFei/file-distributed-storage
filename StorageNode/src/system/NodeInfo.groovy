package system

import groovy.transform.ToString

@ToString(includeNames = true)
class NodeInfo implements Comparable,Serializable{
    String  name
    String  address
    Integer port
    Double  ratio = 0
    Date    alive
    
    @Override
    int compareTo(Object o){
        return this.ratio-o.ratio
    }
}