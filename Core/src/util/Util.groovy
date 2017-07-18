package util

class Util{
    
    /**
     * @author BalusC
     */
    static String getHumanReadableByteCount(long bytes, boolean si=false) {
        int unit = si? 1000 : 1024
        if (bytes <unit) return bytes +" B"
        int exp = (int) (Math.log(bytes)/Math.log(unit))
        String pre = (si?"kMGTPE" :"KMGTPE")[exp-1] + (si?"" :"i")
        return String.format("%.1f %sB", bytes/Math.pow(unit, exp), pre)
    }
    
    static Integer getFreePort(){
        ServerSocket free = new ServerSocket(0)
        def port = free.localPort
        free.close()
        return port
    }
    
    static Long getBytesFromFileSizeString(String s){
        def units=['KB','MB','GB','TB']
        for(int i=0;i<units.size();i++){
            if(s.endsWith(units[i])){
                return 1024**(i+1)*((s-units[i]) as Long)
            }
        }
    }
}
