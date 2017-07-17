package hm

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

import java.text.SimpleDateFormat

class Application extends GrailsAutoConfiguration {
    
    public static def timeFormat=new SimpleDateFormat('yyyy-MM-dd a h:mm',Locale.CHINA)
    public static def fileTimeFormat=new SimpleDateFormat('yyyy-MM-dd-a-h-mm',Locale.CHINA)
    
public static File projectDir
        
    static{
        projectDir=new File(System.properties['user.dir'] as String)
        println "当前路径： $projectDir.absolutePath"
    }
    
    @Override
    void doWithApplicationContext(){
        println config
    }
    
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
    