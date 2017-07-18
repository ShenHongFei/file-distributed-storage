package system

import groovy.time.TimeCategory
import groovy.transform.ToString

@ToString(includeNames = true)
class NodeInfo implements Comparable,Serializable{
    String  name
    String  address
    Integer port
    Long totalSize=0
    Long usedSize=0
    Date    alive
    
    @Override
    int compareTo(Object o){
        (this.ratio-o.ratio)<=0?-1:1
    }
    
    Boolean isAliveNow(){
        use(TimeCategory){
            (alive<=>18.seconds.ago)>0
        }
    }
    
    Long getFreeSize(){
        totalSize-usedSize
    }
    
    Double getRatio(){
        usedSize/totalSize
    }
}