set echo on
REM Creating table CorrelationObjectLink for ext.appo.change.models.CorrelationObjectLink
set echo off
CREATE TABLE CorrelationObjectLink (
   ecnBranchIdentifier   VARCHAR2(600),
   linkType   VARCHAR2(600),
   perBranchIdentifier   VARCHAR2(600),
   classnamekeyroleAObjectRef   VARCHAR2(600),
   idA3A5   NUMBER,
   classnamekeyroleBObjectRef   VARCHAR2(600),
   idA3B5   NUMBER,
   createStampA2   DATE,
   markForDeleteA2   NUMBER NOT NULL,
   modifyStampA2   DATE,
   classnameA2A2   VARCHAR2(600),
   idA2A2   NUMBER NOT NULL,
   updateCountA2   NUMBER,
   updateStampA2   DATE,
 CONSTRAINT PK_CorrelationObjectLink PRIMARY KEY (idA2A2))
 STORAGE ( INITIAL 20k NEXT 20k PCTINCREASE 0 )
ENABLE PRIMARY KEY USING INDEX
 TABLESPACE INDX
 STORAGE ( INITIAL 20k NEXT 20k PCTINCREASE 0 )
/
COMMENT ON TABLE CorrelationObjectLink IS 'Table CorrelationObjectLink created for ext.appo.change.models.CorrelationObjectLink'
/
REM @//ext/appo/change/models/CorrelationObjectLink_UserAdditions
