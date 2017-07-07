package system

import groovy.transform.ToString

@ToString(includeNames = true)
class Command implements Serializable{
    String method
    String[] args
}
