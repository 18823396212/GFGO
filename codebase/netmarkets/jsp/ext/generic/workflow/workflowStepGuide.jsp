<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
   
   <jsp:include page="${mvc:getComponentURL('ext.generic.workflow.builder.WorkflowStepGuidePanelBuilder')}" flush="true"/>


<style type="text/css">

   .guideFontStyle{
		   text-align:center;
		   font-family:Arial,Helvetica,sans-serif;
	     font-size:10px;
			 	background:-webkit-gradient(linear, left top, left bottom, color-start(0.05, #ededed), color-stop(1, #f5f5f5));
			  background:-moz-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
			  background:-o-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
			  background:-ms-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
			  background:linear-gradient(to bottom, #ededed 5%, #f5f5f5 100%);
			  background:-webkit-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	     vertical-align:top;
	     font-weight:normal
		  
		   line-height:20px;
		   padding-top:0px;
   }

  .guideRunningActiveStyle{
	  line-height:50px;
	  height:50px;
	  width:150px;
	  color: #00ff20;
	  background-color:#ededed;
	  font-weight: bold;
	  font-size: large ;
	  font-family:Arial;
	  background:-webkit-gradient(linear, left top, left bottom, color-start(0.05, #ededed), color-stop(1, #f5f5f5));
	  background:-moz-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-o-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-ms-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:linear-gradient(to bottom, #ededed 5%, #f5f5f5 100%);
	  background:-webkit-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#f5f5f5',GradientType=0);
	  border:1px solid #dcdcdc;
	  -webkit-border-top-left-radius:6px;
	  -moz-border-radius-topleft:6px;
	  border-top-left-radius:6px;
	  -webkit-border-top-right-radius:6px;
	  -moz-border-radius-topright:6px;
	  border-top-right-radius:6px;
	  -webkit-border-bottom-left-radius:6px;
	  -moz-border-radius-bottomleft:6px;
	  border-bottom-left-radius:6px;
	  -webkit-border-bottom-right-radius:6px;
	  -moz-border-radius-bottomright:6px;
	  border-bottom-right-radius:6px;
	  -moz-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  -webkit-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  box-shadow: inset 0px 0px 0px 0px #ffffff;
	  text-align:center;
	  display:inline-block;
	  text-decoration:none;
	}
	
  .guideWaitingActiveStyle{
	  line-height:50px;
	  height:50px;
	  width:150px;
	  color: #cdcdcd;
	  background-color:#ededed;
	  font-weight: bold;
	  font-size: large ;
	  font-family:Arial;
	  background:-webkit-gradient(linear, left top, left bottom, color-start(0.05, #ededed), color-stop(1, #f5f5f5));
	  background:-moz-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-o-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-ms-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:linear-gradient(to bottom, #ededed 5%, #f5f5f5 100%);
	  background:-webkit-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#f5f5f5',GradientType=0);
	  border:1px solid #dcdcdc;
	  -webkit-border-top-left-radius:6px;
	  -moz-border-radius-topleft:6px;
	  border-top-left-radius:6px;
	  -webkit-border-top-right-radius:6px;
	  -moz-border-radius-topright:6px;
	  border-top-right-radius:6px;
	  -webkit-border-bottom-left-radius:6px;
	  -moz-border-radius-bottomleft:6px;
	  border-bottom-left-radius:6px;
	  -webkit-border-bottom-right-radius:6px;
	  -moz-border-radius-bottomright:6px;
	  border-bottom-right-radius:6px;
	  -moz-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  -webkit-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  box-shadow: inset 0px 0px 0px 0px #ffffff;
	  text-align:center;
	  display:inline-block;
	  text-decoration:none;
	}
	
  .guideCompletedActiveStyle{
	  line-height:50px;
	  height:50px;
	  width:150px;
	  color: #000000;
	  background-color:#ededed;
	  font-weight: bold;
	  font-size: large ;
	  font-family:Arial;
	  background:-webkit-gradient(linear, left top, left bottom, color-start(0.05, #ededed), color-stop(1, #f5f5f5));
	  background:-moz-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-o-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:-ms-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  background:linear-gradient(to bottom, #ededed 5%, #f5f5f5 100%);
	  background:-webkit-linear-gradient(top, #ededed 5%, #f5f5f5 100%);
	  filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#f5f5f5',GradientType=0);
	  border:1px solid #dcdcdc;
	  -webkit-border-top-left-radius:6px;
	  -moz-border-radius-topleft:6px;
	  border-top-left-radius:6px;
	  -webkit-border-top-right-radius:6px;
	  -moz-border-radius-topright:6px;
	  border-top-right-radius:6px;
	  -webkit-border-bottom-left-radius:6px;
	  -moz-border-radius-bottomleft:6px;
	  border-bottom-left-radius:6px;
	  -webkit-border-bottom-right-radius:6px;
	  -moz-border-radius-bottomright:6px;
	  border-bottom-right-radius:6px;
	  -moz-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  -webkit-box-shadow: inset 0px 0px 0px 0px #ffffff;
	  box-shadow: inset 0px 0px 0px 0px #ffffff;
	  text-align:center;
	  display:inline-block;
	  text-decoration:none;
	}
	
	#fontStyle { 
			color:rgb(131, 133, 119);
			font-size:20px;
			padding-top:10px;
			padding-bottom:10px;
			padding-left:9px;
			padding-right:9px;
			border-width:1.6px;
			border-color:rgb(250, 250, 250);
			border-style:solid;
			border-radius:0px;
			background-color:rgb(214, 219, 206);
		}

</style>
  	