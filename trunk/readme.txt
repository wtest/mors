1. run
java -jar mors.jar [args] <document_directory>
<document_directory>为待检测文档所在的目录，能够检测嵌套目录下的文件。目前只支持txt、doc、pdf、html这四种类型的文件，如果该文件夹下包含有其他文件类型，有可能会出错。
程序运行时会在mors.jar所在的文件夹下，生成一些临时文件，比如索引、下载的网页等

2. arguments
-l	在本地文件之间检索雷同，参数为字符串，指定本地结果文件。
-w	在互联网范围内检索雷同，参数为字符串，指定网络结果文件。
-t	指定运行时的线程数，参数为正整数，建议设为10。
-T	指定阈值，两篇文档相似度大于阈值则被认为是雷同，参数为0到1之间的小数，建议设为0.3。

3. example
java -jar mors.jar -l result-local.txt -w result-web.txt -T 0.3 papers