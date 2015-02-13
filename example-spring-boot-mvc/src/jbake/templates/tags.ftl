<#include "header.ftl">

	<#include "menu.ftl">
	
	<div class="page-header">
		<h1>Tag: ${tag}</h1>
	</div>
	
	<!--<ul>-->
		<#list tag_posts as post>
		<#if (last_month)??>
			<#if post.date?string("MMMM yyyy") != last_month>
				</ul>
				<h4>${post.date?string("MMMM yyyy")}</h4>
				<ul>
			</#if>
		<#else>
			<h4>${post.date?string("MMMM yyyy")}</h4>
			<ul>
		</#if>
		
		<li>${post.date?string("dd")} - <a href="${post.uri}">${post.title}</a></li>
		<#assign last_month = post.date?string("MMMM yyyy")>
		</#list>
	</ul>
	
<#include "footer.ftl">