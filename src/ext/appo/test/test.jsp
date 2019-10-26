<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here  add table</title>
  <script type="text/javascript" src="http://sandbox.runjs.cn/uploads/rs/289/za0sqcyf/jquery-1.6.min.js"></script>
        <script type="text/javascript">
            $(function(){
                $("button").click(function(){
                    var html = "<tr><td style='border:1px solid white;'>添加一行</td><td style='border:1px solid white;'>添加一行</td></tr>";   //自己定义好要添加的信息
                    $("table").append(html);  //添加对应的内容到table
                });
            });
        </script>
</head>
<body>
   <button>Hello </button>
        <table style="border:1px solid yellow;">
           <tr><td style='border:1px solid white;'>添加一行</td><td style='border:1px solid white;'>添加一行</td></tr>
           <tr><td style='border:1px solid white;'>添加一行</td><td style='border:1px solid white;'>添加一行</td></tr>
           <tr><td style='border:1px solid white;'>添加一行</td><td style='border:1px solid white;'>添加一行</td></tr>  
        </table>
</body>
</html>