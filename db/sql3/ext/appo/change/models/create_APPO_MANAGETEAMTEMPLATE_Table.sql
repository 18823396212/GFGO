CREATE TABLE APPO_MANAGETEAMTEMPLATE (
   templateOid     VARCHAR2(600),
    templateName    VARCHAR2(600),
    showTemplate    VARCHAR2(200),
    shareTemplate   VARCHAR2(200),
    userName        VARCHAR2(200),
    userFullName    VARCHAR2(200),
    CLASSNAMEKEYA4       VARCHAR2(600),
    IDA3A4               NUMBER,
    CLASSNAMEA2A2   VARCHAR2(600),
    idA2A2          NUMBER NOT NULL,
    CREATESTAMPA2              DATE,
    MARKFORDELETEA2            NUMBER not null,
    MODIFYSTAMPA2              DATE,
    UPDATECOUNTA2              NUMBER,
    UPDATESTAMPA2              DATE,
    CONSTRAINT PK_APPO_MANAGETEAMTEMPLATE PRIMARY KEY (idA2A2)
    );


