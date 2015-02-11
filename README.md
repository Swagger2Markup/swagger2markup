# swagger2AsciiDoc
This is a Swagger to AsciiDoc converter. It takes an swagger.json or swagger.yaml as an input file 
and generates an AsciiDoc file.

```
Swagger2AsciiDocConverter.newInstance("/tmp/swagger.json", "/tmp/swagger.adoc").convertSwagger2AsciiDoc();
```

Example swagger.json
![](https://github.com/RobWin/swagger2AsciiDoc/blob/master/images/swagger_json.PNG)

Example generated AsciiDoc file
![](https://github.com/RobWin/swagger2AsciiDoc/blob/master/images/asciidoc.PNG)

Example generated HTML
![](https://github.com/RobWin/swagger2AsciiDoc/blob/master/images/asciidoc_html.PNG)
