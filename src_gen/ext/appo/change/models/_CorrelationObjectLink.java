package ext.appo.change.models;

@SuppressWarnings({"cast", "deprecation", "rawtypes", "unchecked"})
public abstract class _CorrelationObjectLink extends wt.fc.ObjectToObjectLink implements java.io.Externalizable {
   static final long serialVersionUID = 1;

   static final java.lang.String RESOURCE = "ext.appo.change.models.modelsResource";
   static final java.lang.String CLASSNAME = CorrelationObjectLink.class.getName();

   /**
    * ECN对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String ECN_BRANCH_IDENTIFIER = "ecnBranchIdentifier";
   static int ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = -1;
   java.lang.String ecnBranchIdentifier;
   /**
    * ECN对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getEcnBranchIdentifier() {
      return ecnBranchIdentifier;
   }
   /**
    * ECN对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setEcnBranchIdentifier(java.lang.String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
      ecnBranchIdentifierValidate(ecnBranchIdentifier);
      this.ecnBranchIdentifier = ecnBranchIdentifier;
   }
   void ecnBranchIdentifierValidate(java.lang.String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
      if (ECN_BRANCH_IDENTIFIER_UPPER_LIMIT < 1) {
         try { ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("ecnBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (ecnBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(ecnBranchIdentifier.toString(), ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "ecnBranchIdentifier"), java.lang.String.valueOf(java.lang.Math.min(ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "ecnBranchIdentifier", this.ecnBranchIdentifier, ecnBranchIdentifier));
   }

   /**
    * Persistable对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String PER_BRANCH_IDENTIFIER = "perBranchIdentifier";
   static int PER_BRANCH_IDENTIFIER_UPPER_LIMIT = -1;
   java.lang.String perBranchIdentifier;
   /**
    * Persistable对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getPerBranchIdentifier() {
      return perBranchIdentifier;
   }
   /**
    * Persistable对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setPerBranchIdentifier(java.lang.String perBranchIdentifier) throws wt.util.WTPropertyVetoException {
      perBranchIdentifierValidate(perBranchIdentifier);
      this.perBranchIdentifier = perBranchIdentifier;
   }
   void perBranchIdentifierValidate(java.lang.String perBranchIdentifier) throws wt.util.WTPropertyVetoException {
      if (PER_BRANCH_IDENTIFIER_UPPER_LIMIT < 1) {
         try { PER_BRANCH_IDENTIFIER_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("perBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { PER_BRANCH_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (perBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(perBranchIdentifier.toString(), PER_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "perBranchIdentifier"), java.lang.String.valueOf(java.lang.Math.min(PER_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "perBranchIdentifier", this.perBranchIdentifier, perBranchIdentifier));
   }

   /**
    * ECA对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String ECA_IDENTIFIER = "ecaIdentifier";
   static int ECA_IDENTIFIER_UPPER_LIMIT = -1;
   java.lang.String ecaIdentifier;
   /**
    * ECA对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getEcaIdentifier() {
      return ecaIdentifier;
   }
   /**
    * ECA对象VID
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setEcaIdentifier(java.lang.String ecaIdentifier) throws wt.util.WTPropertyVetoException {
      ecaIdentifierValidate(ecaIdentifier);
      this.ecaIdentifier = ecaIdentifier;
   }
   void ecaIdentifierValidate(java.lang.String ecaIdentifier) throws wt.util.WTPropertyVetoException {
      if (ECA_IDENTIFIER_UPPER_LIMIT < 1) {
         try { ECA_IDENTIFIER_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("ecaIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ECA_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (ecaIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(ecaIdentifier.toString(), ECA_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "ecaIdentifier"), java.lang.String.valueOf(java.lang.Math.min(ECA_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "ecaIdentifier", this.ecaIdentifier, ecaIdentifier));
   }

   /**
    * 类型
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String LINK_TYPE = "linkType";
   static int LINK_TYPE_UPPER_LIMIT = -1;
   java.lang.String linkType;
   /**
    * 类型
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getLinkType() {
      return linkType;
   }
   /**
    * 类型
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setLinkType(java.lang.String linkType) throws wt.util.WTPropertyVetoException {
      linkTypeValidate(linkType);
      this.linkType = linkType;
   }
   void linkTypeValidate(java.lang.String linkType) throws wt.util.WTPropertyVetoException {
      if (LINK_TYPE_UPPER_LIMIT < 1) {
         try { LINK_TYPE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("linkType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { LINK_TYPE_UPPER_LIMIT = 200; }
      }
      if (linkType != null && !wt.fc.PersistenceHelper.checkStoredLength(linkType.toString(), LINK_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "linkType"), java.lang.String.valueOf(java.lang.Math.min(LINK_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "linkType", this.linkType, linkType));
   }

   /**
    * 更改详细描述
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String AAD_DESCRIPTION = "aadDescription";
   static int AAD_DESCRIPTION_UPPER_LIMIT = -1;
   java.lang.String aadDescription;
   /**
    * 更改详细描述
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getAadDescription() {
      return aadDescription;
   }
   /**
    * 更改详细描述
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setAadDescription(java.lang.String aadDescription) throws wt.util.WTPropertyVetoException {
      aadDescriptionValidate(aadDescription);
      this.aadDescription = aadDescription;
   }
   void aadDescriptionValidate(java.lang.String aadDescription) throws wt.util.WTPropertyVetoException {
      if (AAD_DESCRIPTION_UPPER_LIMIT < 1) {
         try { AAD_DESCRIPTION_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("aadDescription").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { AAD_DESCRIPTION_UPPER_LIMIT = 2000; }
      }
      if (aadDescription != null && !wt.fc.PersistenceHelper.checkStoredLength(aadDescription.toString(), AAD_DESCRIPTION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "aadDescription"), java.lang.String.valueOf(java.lang.Math.min(AAD_DESCRIPTION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "aadDescription", this.aadDescription, aadDescription));
   }

   /**
    * 路由
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String ROUTING = "routing";
   static int ROUTING_UPPER_LIMIT = -1;
   java.lang.String routing;
   /**
    * 路由
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getRouting() {
      return routing;
   }
   /**
    * 路由
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setRouting(java.lang.String routing) throws wt.util.WTPropertyVetoException {
      routingValidate(routing);
      this.routing = routing;
   }
   void routingValidate(java.lang.String routing) throws wt.util.WTPropertyVetoException {
      if (ROUTING_UPPER_LIMIT < 1) {
         try { ROUTING_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("routing").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ROUTING_UPPER_LIMIT = 200; }
      }
      if (routing != null && !wt.fc.PersistenceHelper.checkStoredLength(routing.toString(), ROUTING_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "routing"), java.lang.String.valueOf(java.lang.Math.min(ROUTING_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "routing", this.routing, routing));
   }

   /**
    * 审批意见
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String APPROVAL_OPINION = "approvalOpinion";
   static int APPROVAL_OPINION_UPPER_LIMIT = -1;
   java.lang.String approvalOpinion;
   /**
    * 审批意见
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getApprovalOpinion() {
      return approvalOpinion;
   }
   /**
    * 审批意见
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setApprovalOpinion(java.lang.String approvalOpinion) throws wt.util.WTPropertyVetoException {
      approvalOpinionValidate(approvalOpinion);
      this.approvalOpinion = approvalOpinion;
   }
   void approvalOpinionValidate(java.lang.String approvalOpinion) throws wt.util.WTPropertyVetoException {
      if (APPROVAL_OPINION_UPPER_LIMIT < 1) {
         try { APPROVAL_OPINION_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("approvalOpinion").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { APPROVAL_OPINION_UPPER_LIMIT = 200; }
      }
      if (approvalOpinion != null && !wt.fc.PersistenceHelper.checkStoredLength(approvalOpinion.toString(), APPROVAL_OPINION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "approvalOpinion"), java.lang.String.valueOf(java.lang.Math.min(APPROVAL_OPINION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "approvalOpinion", this.approvalOpinion, approvalOpinion));
   }

   /**
    * 备注（驳回必填）
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String REMARK = "remark";
   static int REMARK_UPPER_LIMIT = -1;
   java.lang.String remark;
   /**
    * 备注（驳回必填）
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public java.lang.String getRemark() {
      return remark;
   }
   /**
    * 备注（驳回必填）
    *
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setRemark(java.lang.String remark) throws wt.util.WTPropertyVetoException {
      remarkValidate(remark);
      this.remark = remark;
   }
   void remarkValidate(java.lang.String remark) throws wt.util.WTPropertyVetoException {
      if (REMARK_UPPER_LIMIT < 1) {
         try { REMARK_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("remark").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { REMARK_UPPER_LIMIT = 2000; }
      }
      if (remark != null && !wt.fc.PersistenceHelper.checkStoredLength(remark.toString(), REMARK_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "remark"), java.lang.String.valueOf(java.lang.Math.min(REMARK_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "remark", this.remark, remark));
   }

   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String CHANGE_ORDER2_ROLE = "changeOrder2";
   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public wt.change2.WTChangeOrder2 getChangeOrder2() {
      return (wt.change2.WTChangeOrder2) getRoleAObject();
   }
   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setChangeOrder2(wt.change2.WTChangeOrder2 the_changeOrder2) throws wt.util.WTPropertyVetoException {
      setRoleAObject((wt.fc.Persistable) the_changeOrder2);
   }

   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public static final java.lang.String PERSISTABLE_ROLE = "persistable";
   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public wt.fc.Persistable getPersistable() {
      return (wt.fc.Persistable) getRoleBObject();
   }
   /**
    * @see ext.appo.change.models.CorrelationObjectLink
    */
   public void setPersistable(wt.fc.Persistable the_persistable) throws wt.util.WTPropertyVetoException {
      setRoleBObject((wt.fc.Persistable) the_persistable);
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

   public static final long EXTERNALIZATION_VERSION_UID = -8762476992891204370L;

   public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
      output.writeLong( EXTERNALIZATION_VERSION_UID );

      super.writeExternal( output );

      output.writeObject( aadDescription );
      output.writeObject( approvalOpinion );
      output.writeObject( ecaIdentifier );
      output.writeObject( ecnBranchIdentifier );
      output.writeObject( linkType );
      output.writeObject( perBranchIdentifier );
      output.writeObject( remark );
      output.writeObject( routing );
   }

   protected void super_writeExternal_CorrelationObjectLink(java.io.ObjectOutput output) throws java.io.IOException {
      super.writeExternal(output);
   }

   public void readExternal(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      long readSerialVersionUID = input.readLong();
      readVersion( (ext.appo.change.models.CorrelationObjectLink) this, input, readSerialVersionUID, false, false );
   }
   protected void super_readExternal_CorrelationObjectLink(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      super.readExternal(input);
   }

   public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.writeExternal( output );

      output.setString( "aadDescription", aadDescription );
      output.setString( "approvalOpinion", approvalOpinion );
      output.setString( "ecaIdentifier", ecaIdentifier );
      output.setString( "ecnBranchIdentifier", ecnBranchIdentifier );
      output.setString( "linkType", linkType );
      output.setString( "perBranchIdentifier", perBranchIdentifier );
      output.setString( "remark", remark );
      output.setString( "routing", routing );
   }

   public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.readExternal( input );

      aadDescription = input.getString( "aadDescription" );
      approvalOpinion = input.getString( "approvalOpinion" );
      ecaIdentifier = input.getString( "ecaIdentifier" );
      ecnBranchIdentifier = input.getString( "ecnBranchIdentifier" );
      linkType = input.getString( "linkType" );
      perBranchIdentifier = input.getString( "perBranchIdentifier" );
      remark = input.getString( "remark" );
      routing = input.getString( "routing" );
   }

   boolean readVersion_8762476992891204370L( java.io.ObjectInput input, long readSerialVersionUID, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      if ( !superDone )
         super.readExternal( input );

      aadDescription = (java.lang.String) input.readObject();
      approvalOpinion = (java.lang.String) input.readObject();
      ecaIdentifier = (java.lang.String) input.readObject();
      ecnBranchIdentifier = (java.lang.String) input.readObject();
      linkType = (java.lang.String) input.readObject();
      perBranchIdentifier = (java.lang.String) input.readObject();
      remark = (java.lang.String) input.readObject();
      routing = (java.lang.String) input.readObject();
      return true;
   }

   protected boolean readVersion( CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      boolean success = true;

      if ( readSerialVersionUID == EXTERNALIZATION_VERSION_UID )
         return readVersion_8762476992891204370L( input, readSerialVersionUID, superDone );
      else
         success = readOldVersion( input, readSerialVersionUID, passThrough, superDone );

      if (input instanceof wt.pds.PDSObjectInput)
         wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

      return success;
   }
   protected boolean super_readVersion_CorrelationObjectLink( _CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
   }

   boolean readOldVersion( java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID="+readSerialVersionUID+" local class externalizationVersionUID="+EXTERNALIZATION_VERSION_UID);
   }
}
