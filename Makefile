JSOUP = jsoup-1.10.2.jar

bank.jar: Account.java bank.java
	CLASSPATH=$(JSOUP) javac Account.java
	-mkdir org
	-mkdir org/skhanderson
	cp Account.class org/skhanderson
	CLASSPATH=$(JSOUP):. javac bank.java
	jar cf bank.jar bank.class org
run:
	CLASSPATH=bank.jar:$(JSOUP) java bank log.html
