# dfs

This project is a simple implementation of DFS with II-phase commit protocol.

### System requirements
1. Linux Operating System
2. JDK/JRE 1.8+
3. Apache ant runtime

### How to build
Download Google Gson library, and copy its JAR to <code>lib</code> directory.<br>
Navigate to the root directory, and use ant runtime.
<pre>
<code>
$ cd dfs
$ ant
</code>
</pre>

### How to run

All configurations are stored in <a href='resources/config.properties'>config</a> file.<br>
Change the number of servers and clients to run accordingly.
<br><br>
To start a server with ID = 0:
<pre>
<code>
$ ./run.sh server 0
</code>
</pre>
<br>
To start a client with ID = 1:
<pre>
<code>
$ ./run.sh client 1
</code>
</pre>
