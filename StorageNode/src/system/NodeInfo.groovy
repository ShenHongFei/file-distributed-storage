package system

import groovy.time.TimeCategory
import groovy.transform.ToString

@ToString(includeNames = true)
class NodeInfo implements Comparable,Serializable{
    String  name
    String  address
    Integer port
    Double  ratio = 0
    Long totalSize=0
    Long usedSize=0
    Date    alive
    
    
    @Override
    int compareTo(Object o){
        (this.ratio-o.ratio) as Integer
    }
    
    Boolean isAliveNow(){
        use(TimeCategory){
            (alive<=>30.seconds.ago)>0
        }
    }
}