package hm

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

import java.text.SimpleDateFormat

class Application extends GrailsAutoConfiguration {
        
    public static def timeFormat=new SimpleDateFormat('yyyy-MM-dd a h:mm',Locale.CHINA)
    public static def fileTimeFormat=new SimpleDateFormat('yyyy-MM-dd-a-h-mm',Locale.CHINA)
    
    public static Properties config =new Properties()
    
public static File projectDir
    public static File webDir
    
    static{
        projectDir=new File(System.properties['user.dir'] as String)
        println "当前路径： $projectDir.absolutePath"
        (webDir=new File(projectDir,'web')).mkdir()
        config.load(new FileInputStream('cfg/Monitor/Monitor1.properties'))
    }
    
    
    @Override
    void doWithApplicationContext(){
        println config
    }
    
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
    