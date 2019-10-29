set echo on
REM Creating table GFGO_TRANSACTIONTASK for ext.appo.change.models.TransactionTask
set echo off
CREATE TABLE GFGO_TRANSACTIONTASK (
   changeActivity2   VARCHAR2(600),
   changeDescribe   VARCHAR2(600),
   changeTheme   VARCHAR2(600),
   needDate   VARCHAR2(600),
   responsible   VARCHAR2(600),
   createStampA2   DATE,
   markForDeleteA2   NUMBER NOT NULL,
   modifyStampA2   DATE,
   classnameA2A2   VARCHAR2(600),
   idA2A2   NUMBER NOT NULL,
   updateCountA2   NUMBER,
   updateStampA2   DATE,
 CONSTRAINT PK_GFGO_TRANSACTIONTASK PRIMARY KEY (idA2A2))
 STORAGE ( INITIAL 20k NEXT 20k PCTINCREASE 0 )
ENABLE PRIMARY KEY USING INDEX
 TABLESPACE INDX
 STORAGE ( INITIAL 20k NEXT 20k PCTINCREASE 0 )
/
COMMENT ON TABLE GFGO_TRANSACTIONTASK IS 'Table GFGO_TRANSACTIONTASK created for ext.appo.change.models.TransactionTask'
/
REM @//ext/appo/change/models/GFGO_TRANSACTIONTASK_UserAdditions
