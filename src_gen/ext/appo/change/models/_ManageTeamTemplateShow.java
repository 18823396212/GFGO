package ext.appo.change.models;

@SuppressWarnings({"cast", "deprecation", "rawtypes", "unchecked"})
public abstract class _ManageTeamTemplateShow extends wt.fc.WTObject implements java.io.Externalizable {
   static final long serialVersionUID = 1;

   static final java.lang.String RESOURCE = "ext.appo.change.models.modelsResource";
   static final java.lang.String CLASSNAME = ManageTeamTemplateShow.class.getName();

   /**
    * 模板ID
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String TEMPLATE_OID = "templateOid";
   static int TEMPLATE_OID_UPPER_LIMIT = -1;
   java.lang.String templateOid;
   /**
    * 模板ID
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public java.lang.String getTemplateOid() {
      return templateOid;
   }
   /**
    * 模板ID
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setTemplateOid(java.lang.String templateOid) throws wt.util.WTPropertyVetoException {
      templateOidValidate(templateOid);
      this.templateOid = templateOid;
   }
   void templateOidValidate(java.lang.String templateOid) throws wt.util.WTPropertyVetoException {
      if (TEMPLATE_OID_UPPER_LIMIT < 1) {
         try { TEMPLATE_OID_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("templateOid").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { TEMPLATE_OID_UPPER_LIMIT = 200; }
      }
      if (templateOid != null && !wt.fc.PersistenceHelper.checkStoredLength(templateOid.toString(), TEMPLATE_OID_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "templateOid"), java.lang.String.valueOf(java.lang.Math.min(TEMPLATE_OID_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "templateOid", this.templateOid, templateOid));
   }

   /**
    * 模板名称
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String TEMPLATE_NAME = "templateName";
   static int TEMPLATE_NAME_UPPER_LIMIT = -1;
   java.lang.String templateName;
   /**
    * 模板名称
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public java.lang.String getTemplateName() {
      return templateName;
   }
   /**
    * 模板名称
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setTemplateName(java.lang.String templateName) throws wt.util.WTPropertyVetoException {
      templateNameValidate(templateName);
      this.templateName = templateName;
   }
   void templateNameValidate(java.lang.String templateName) throws wt.util.WTPropertyVetoException {
      if (TEMPLATE_NAME_UPPER_LIMIT < 1) {
         try { TEMPLATE_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("templateName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { TEMPLATE_NAME_UPPER_LIMIT = 200; }
      }
      if (templateName != null && !wt.fc.PersistenceHelper.checkStoredLength(templateName.toString(), TEMPLATE_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "templateName"), java.lang.String.valueOf(java.lang.Math.min(TEMPLATE_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "templateName", this.templateName, templateName));
   }

   /**
    * 显示
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String SHOW_TEMPLATE = "showTemplate";
   static int SHOW_TEMPLATE_UPPER_LIMIT = -1;
   java.lang.String showTemplate;
   /**
    * 显示
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public java.lang.String getShowTemplate() {
      return showTemplate;
   }
   /**
    * 显示
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setShowTemplate(java.lang.String showTemplate) throws wt.util.WTPropertyVetoException {
      showTemplateValidate(showTemplate);
      this.showTemplate = showTemplate;
   }
   void showTemplateValidate(java.lang.String showTemplate) throws wt.util.WTPropertyVetoException {
      if (SHOW_TEMPLATE_UPPER_LIMIT < 1) {
         try { SHOW_TEMPLATE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("showTemplate").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { SHOW_TEMPLATE_UPPER_LIMIT = 200; }
      }
      if (showTemplate != null && !wt.fc.PersistenceHelper.checkStoredLength(showTemplate.toString(), SHOW_TEMPLATE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "showTemplate"), java.lang.String.valueOf(java.lang.Math.min(SHOW_TEMPLATE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "showTemplate", this.showTemplate, showTemplate));
   }

   /**
    * 用户名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String USER_NAME = "userName";
   static int USER_NAME_UPPER_LIMIT = -1;
   java.lang.String userName;
   /**
    * 用户名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public java.lang.String getUserName() {
      return userName;
   }
   /**
    * 用户名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setUserName(java.lang.String userName) throws wt.util.WTPropertyVetoException {
      userNameValidate(userName);
      this.userName = userName;
   }
   void userNameValidate(java.lang.String userName) throws wt.util.WTPropertyVetoException {
      if (USER_NAME_UPPER_LIMIT < 1) {
         try { USER_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("userName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { USER_NAME_UPPER_LIMIT = 200; }
      }
      if (userName != null && !wt.fc.PersistenceHelper.checkStoredLength(userName.toString(), USER_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "userName"), java.lang.String.valueOf(java.lang.Math.min(USER_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "userName", this.userName, userName));
   }

   /**
    * 用户全名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String USER_FULL_NAME = "userFullName";
   static int USER_FULL_NAME_UPPER_LIMIT = -1;
   java.lang.String userFullName;
   /**
    * 用户全名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public java.lang.String getUserFullName() {
      return userFullName;
   }
   /**
    * 用户全名
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setUserFullName(java.lang.String userFullName) throws wt.util.WTPropertyVetoException {
      userFullNameValidate(userFullName);
      this.userFullName = userFullName;
   }
   void userFullNameValidate(java.lang.String userFullName) throws wt.util.WTPropertyVetoException {
      if (USER_FULL_NAME_UPPER_LIMIT < 1) {
         try { USER_FULL_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("userFullName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { USER_FULL_NAME_UPPER_LIMIT = 200; }
      }
      if (userFullName != null && !wt.fc.PersistenceHelper.checkStoredLength(userFullName.toString(), USER_FULL_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "userFullName"), java.lang.String.valueOf(java.lang.Math.min(USER_FULL_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "userFullName", this.userFullName, userFullName));
   }

   /**
    * 保存用户
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public static final java.lang.String SAVE_USER = "saveUser";
   wt.fc.ObjectReference saveUser;
   /**
    * 保存用户
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public wt.fc.ObjectReference getSaveUser() {
      return saveUser;
   }
   /**
    * 保存用户
    *
    * @see ext.appo.change.models.ManageTeamTemplateShow
    */
   public void setSaveUser(wt.fc.ObjectReference saveUser) throws wt.util.WTPropertyVetoException {
      saveUserValidate(saveUser);
      this.saveUser = saveUser;
   }
   void saveUserValidate(wt.fc.ObjectReference saveUser) throws wt.util.WTPropertyVetoException {
   }

   public java.lang.String getConceptualClassname() {
      return CLASSNAME;
   }

   public wt.introspection.ClassInfo getClassInfo() throws wt.introspection.WTIntrospectionException {
      return wt.introspection.WTIntrospector.getClassInfo(getConceptualClassname());
   }

   public java.lang.String getType() {
      try { return getClassInfo().getDisplayName(); }
      catch (wt.introspection.WTIntrospectionException wte) { return wt.util.WTStringUtilities.tail(getConceptualClassname(), '.'); }
   }

   public static final long EXTERNALIZATION_VERSION_UID = 4401189343462502166L;

   public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
      output.writeLong( EXTERNALIZATION_VERSION_UID );

      super.writeExternal( output );

      output.writeObject( saveUser );
      output.writeObject( showTemplate );
      output.writeObject( templateName );
      output.writeObject( templateOid );
      output.writeObject( userFullName );
      output.writeObject( userName );
   }

   protected void super_writeExternal_ManageTeamTemplateShow(java.io.ObjectOutput output) throws java.io.IOException {
      super.writeExternal(output);
   }

   public void readExternal(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      long readSerialVersionUID = input.readLong();
      readVersion( (ext.appo.change.models.ManageTeamTemplateShow) this, input, readSerialVersionUID, false, false );
   }
   protected void super_readExternal_ManageTeamTemplateShow(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      super.readExternal(input);
   }

   public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.writeExternal( output );

      output.writeObject( "saveUser", saveUser, wt.fc.ObjectReference.class, true );
      output.setString( "showTemplate", showTemplate );
      output.setString( "templateName", templateName );
      output.setString( "templateOid", templateOid );
      output.setString( "userFullName", userFullName );
      output.setString( "userName", userName );
   }

   public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.readExternal( input );

      saveUser = (wt.fc.ObjectReference) input.readObject( "saveUser", saveUser, wt.fc.ObjectReference.class, true );
      showTemplate = input.getString( "showTemplate" );
      templateName = input.getString( "templateName" );
      templateOid = input.getString( "templateOid" );
      userFullName = input.getString( "userFullName" );
      userName = input.getString( "userName" );
   }

   boolean readVersion4401189343462502166L( java.io.ObjectInput input, long readSerialVersionUID, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      if ( !superDone )
         super.readExternal( input );

      saveUser = (wt.fc.ObjectReference) input.readObject();
      showTemplate = (java.lang.String) input.readObject();
      templateName = (java.lang.String) input.readObject();
      templateOid = (java.lang.String) input.readObject();
      userFullName = (java.lang.String) input.readObject();
      userName = (java.lang.String) input.readObject();
      return true;
   }

   protected boolean readVersion( ManageTeamTemplateShow thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      boolean success = true;

      if ( readSerialVersionUID == EXTERNALIZATION_VERSION_UID )
         return readVersion4401189343462502166L( input, readSerialVersionUID, superDone );
      else
         success = readOldVersion( input, readSerialVersionUID, passThrough, superDone );

      if (input instanceof wt.pds.PDSObjectInput)
         wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

      return success;
   }
   protected boolean super_readVersion_ManageTeamTemplateShow( _ManageTeamTemplateShow thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
   }

   boolean readOldVersion( java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID="+readSerialVersionUID+" local class externalizationVersionUID="+EXTERNALIZATION_VERSION_UID);
   }
}
