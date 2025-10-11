# Makefile compatible Unix/Windows

JAVAC = javac
JAR = jar
SRC_DIR = src
ETUDE_DIR = EtudeExperimental
BIN_DIR = bin

all: jar

compile:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SRC_DIR)/Launcher/*.java $(SRC_DIR)/Regex/*.java $(SRC_DIR)/NDFA/*.java $(SRC_DIR)/DFA/*.java $(SRC_DIR)/Minimisation/*.java $(SRC_DIR)/KMP/*.java $(ETUDE_DIR)/Etude/*.java

jar: compile
	$(JAR) cfe binaire.jar Launcher.Main -C $(BIN_DIR) .

run: jar
	java -jar binaire.jar automate "hello" Samples/56667-0.txt 

clean:
	rm -rf $(BIN_DIR) binaire.jar

.PHONY: all compile jar run clean