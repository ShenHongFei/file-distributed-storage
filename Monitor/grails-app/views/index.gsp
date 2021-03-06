<%@ page import="util.Util" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>系统监控器</title>

    <!-- Bootstrap Core CSS -->
    <link href="vendor/bootstrap/css/bootstrap.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="vendor/metisMenu/metisMenu.css" rel="stylesheet">

    <!-- DataTables CSS -->
    <link href="vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">

    <!-- DataTables Responsive CSS -->
    <link href="vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="dist/css/sb-admin-2.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="vendor/font-awesome/css/font-awesome.css" rel="stylesheet" type="text/css">


</head>

<body>

<div id="wrapper">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="index.html" style="text-align: center;margin: 0px 565px; font-size: 22px;">系统监控器</a>
        </div>
        <!-- /.navbar-header -->

        <ul class="nav navbar-top-links navbar-right">

        </ul>

    </nav>

    <div id="page-wrapper" style="margin-left: 0">
        <%
            def getHumanReadableByteCount ={ Long bytes,si=false->
                int unit = si? 1000 : 1024
                if (bytes <unit) return bytes +" B"
                int exp = (int) (Math.log(bytes)/Math.log(unit))
                String pre = (si?"kMGTPE" :"KMGTPE")[exp-1] + (si?"" :"i")
                return String.format("%.1f %sB", bytes/Math.pow(unit, exp), pre)
            }
        %>
        <div class="row">
            <div class="col-lg-12">
                <h4 class="page-header" style="padding-bottom: 9px; margin: 10px 0 10px;">存储结点</h4>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        存储结点运行状况
                    </div>
                    <!-- /.panel-heading -->
                    <div class="panel-body">
                        <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-example">
                            <thead>
                            <tr>
                                <th>结点名称</th>
                                <th>地址</th>
                                <th>端口</th>
                                <th>在线/离线</th>
                                <th>已用百分比</th>
                                <th>已用存储空间</th>
                                <th>总存储空间</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:set var="count" value="${0}"/>
                            <g:each in="${nodes.values()}" var="node">
                                <tr class="${count++%2?'even':'odd'} gradeX">
                                    <td>${node.name}</td>
                                    <td>${node.address}</td>
                                    <td>${node.port}</td>
                                    <td class="center">${node.aliveNow?'在线':'离线'}</td>
                                    <td class="center">${Math.round(node.ratio*100)+'%'}</td>
                                    <td class="center"><%=getHumanReadableByteCount(node.usedSize)  %></td>
                                    <td class="center"><%=getHumanReadableByteCount(node.totalSize)  %></td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.col-lg-12 -->
        </div>

        <div class="row">
            <div class="col-lg-12">
                <h4 class="page-header" style="padding-bottom: 9px; margin: 10px 0 10px;">文件列表</h4>
            </div>
            <!-- /.col-lg-12 -->
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        文件服务器中存储的文件信息
                    </div>
                    <!-- /.panel-heading -->
                    <div class="panel-body">
                        <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-example2">
                            <thead>
                            <tr>
                                <th>UUID</th>
                                <th>文件名</th>
                                <th>大小</th>
                                <th>主存结点</th>
                                <th>备份结点</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:set var="count2" value="${0}"/>
                            <g:each in="${files.values()}" var="fileInfo">
                                <tr class="${count2++%2?'even':'odd'} gradeX">
                                    <td>${fileInfo.uuid}</td>
                                    <td>${fileInfo.name}</td>
                                    <td class="center"><%=getHumanReadableByteCount(fileInfo.size)  %></td>
                                    <td>${fileInfo?.main?.name}</td>
                                    <td>${fileInfo?.backup?.name}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                    <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
            </div>
            <!-- /.col-lg-12 -->
        </div>
    </div>

</div>
<!-- /#wrapper -->

<!-- jQuery -->
<script src="vendor/jquery/jquery.js"></script>

<!-- Bootstrap Core JavaScript -->
<script src="vendor/bootstrap/js/bootstrap.js"></script>

<!-- Metis Menu Plugin JavaScript -->
<script src="vendor/metisMenu/metisMenu.js"></script>

<!-- DataTables JavaScript -->
<script src="vendor/datatables/js/jquery.dataTables.js"></script>
<script src="vendor/datatables-plugins/dataTables.bootstrap.js"></script>
<script src="vendor/datatables-responsive/dataTables.responsive.js"></script>

<!-- Custom Theme JavaScript -->
<script src="dist/js/sb-admin-2.js"></script>

<!-- Page-Level Demo Scripts - Tables - Use for reference -->
<script>
    $(document).ready(function() {
        $('#dataTables-example').DataTable({
            responsive: true
        });
        $('#dataTables-example2').DataTable({
            responsive: true
        });
    });
</script>

</body>

</html>
