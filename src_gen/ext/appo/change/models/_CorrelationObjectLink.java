package ext.appo.change.models;

@SuppressWarnings({"cast", "deprecation", "rawtypes", "unchecked"})
public abstract class _CorrelationObjectLink extends wt.fc.ObjectToObjectLink implements java.io.Externalizable {
   static final long serialVersionUID = 1;

   static final String RESOURCE = "ext.appo.change.models.modelsResource";
   static final String CLASSNAME = CorrelationObjectLink.class.getName();

   /**
    * ECN对象VID
    *
    * @see CorrelationObjectLink
    */
   public static final String ECN_BRANCH_IDENTIFIER = "ecnBranchIdentifier";
   static int ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = -1;
   String ecnBranchIdentifier;
   /**
    * ECN对象VID
    *
    * @see CorrelationObjectLink
    */
   public String getEcnBranchIdentifier() {
      return ecnBranchIdentifier;
   }
   /**
    * ECN对象VID
    *
    * @see CorrelationObjectLink
    */
   public void setEcnBranchIdentifier(String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
      ecnBranchIdentifierValidate(ecnBranchIdentifier);
      this.ecnBranchIdentifier = ecnBranchIdentifier;
   }
   void ecnBranchIdentifierValidate(String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
      if (ECN_BRANCH_IDENTIFIER_UPPER_LIMIT < 1) {
         try { ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("ecnBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (ecnBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(ecnBranchIdentifier.toString(), ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "ecnBranchIdentifier"), String.valueOf(Math.min(ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "ecnBranchIdentifier", this.ecnBranchIdentifier, ecnBranchIdentifier));
   }

   /**
    * Persistable对象VID
    *
    * @see CorrelationObjectLink
    */
   public static final String PER_BRANCH_IDENTIFIER = "perBranchIdentifier";
   static int PER_BRANCH_IDENTIFIER_UPPER_LIMIT = -1;
   String perBranchIdentifier;
   /**
    * Persistable对象VID
    *
    * @see CorrelationObjectLink
    */
   public String getPerBranchIdentifier() {
      return perBranchIdentifier;
   }
   /**
    * Persistable对象VID
    *
    * @see CorrelationObjectLink
    */
   public void setPerBranchIdentifier(String perBranchIdentifier) throws wt.util.WTPropertyVetoException {
      perBranchIdentifierValidate(perBranchIdentifier);
      this.perBranchIdentifier = perBranchIdentifier;
   }
   void perBranchIdentifierValidate(String perBranchIdentifier) throws wt.util.WTPropertyVetoException {
      if (PER_BRANCH_IDENTIFIER_UPPER_LIMIT < 1) {
         try { PER_BRANCH_IDENTIFIER_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("perBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { PER_BRANCH_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (perBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(perBranchIdentifier.toString(), PER_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "perBranchIdentifier"), String.valueOf(Math.min(PER_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "perBranchIdentifier", this.perBranchIdentifier, perBranchIdentifier));
   }

   /**
    * ECA对象VID
    *
    * @see CorrelationObjectLink
    */
   public static final String ECA_IDENTIFIER = "ecaIdentifier";
   static int ECA_IDENTIFIER_UPPER_LIMIT = -1;
   String ecaIdentifier;
   /**
    * ECA对象VID
    *
    * @see CorrelationObjectLink
    */
   public String getEcaIdentifier() {
      return ecaIdentifier;
   }
   /**
    * ECA对象VID
    *
    * @see CorrelationObjectLink
    */
   public void setEcaIdentifier(String ecaIdentifier) throws wt.util.WTPropertyVetoException {
      ecaIdentifierValidate(ecaIdentifier);
      this.ecaIdentifier = ecaIdentifier;
   }
   void ecaIdentifierValidate(String ecaIdentifier) throws wt.util.WTPropertyVetoException {
      if (ECA_IDENTIFIER_UPPER_LIMIT < 1) {
         try { ECA_IDENTIFIER_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("ecaIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ECA_IDENTIFIER_UPPER_LIMIT = 200; }
      }
      if (ecaIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(ecaIdentifier.toString(), ECA_IDENTIFIER_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "ecaIdentifier"), String.valueOf(Math.min(ECA_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "ecaIdentifier", this.ecaIdentifier, ecaIdentifier));
   }

   /**
    * 类型
    *
    * @see CorrelationObjectLink
    */
   public static final String LINK_TYPE = "linkType";
   static int LINK_TYPE_UPPER_LIMIT = -1;
   String linkType;
   /**
    * 类型
    *
    * @see CorrelationObjectLink
    */
   public String getLinkType() {
      return linkType;
   }
   /**
    * 类型
    *
    * @see CorrelationObjectLink
    */
   public void setLinkType(String linkType) throws wt.util.WTPropertyVetoException {
      linkTypeValidate(linkType);
      this.linkType = linkType;
   }
   void linkTypeValidate(String linkType) throws wt.util.WTPropertyVetoException {
      if (LINK_TYPE_UPPER_LIMIT < 1) {
         try { LINK_TYPE_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("linkType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { LINK_TYPE_UPPER_LIMIT = 200; }
      }
      if (linkType != null && !wt.fc.PersistenceHelper.checkStoredLength(linkType.toString(), LINK_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "linkType"), String.valueOf(Math.min(LINK_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "linkType", this.linkType, linkType));
   }

   /**
    * 更改详细描述
    *
    * @see CorrelationObjectLink
    */
   public static final String AAD_DESCRIPTION = "aadDescription";
   static int AAD_DESCRIPTION_UPPER_LIMIT = -1;
   String aadDescription;
   /**
    * 更改详细描述
    *
    * @see CorrelationObjectLink
    */
   public String getAadDescription() {
      return aadDescription;
   }
   /**
    * 更改详细描述
    *
    * @see CorrelationObjectLink
    */
   public void setAadDescription(String aadDescription) throws wt.util.WTPropertyVetoException {
      aadDescriptionValidate(aadDescription);
      this.aadDescription = aadDescription;
   }
   void aadDescriptionValidate(String aadDescription) throws wt.util.WTPropertyVetoException {
      if (AAD_DESCRIPTION_UPPER_LIMIT < 1) {
         try { AAD_DESCRIPTION_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("aadDescription").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { AAD_DESCRIPTION_UPPER_LIMIT = 2000; }
      }
      if (aadDescription != null && !wt.fc.PersistenceHelper.checkStoredLength(aadDescription.toString(), AAD_DESCRIPTION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "aadDescription"), String.valueOf(Math.min(AAD_DESCRIPTION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "aadDescription", this.aadDescription, aadDescription));
   }

   /**
    * 路由
    *
    * @see CorrelationObjectLink
    */
   public static final String ROUTING = "routing";
   static int ROUTING_UPPER_LIMIT = -1;
   String routing;
   /**
    * 路由
    *
    * @see CorrelationObjectLink
    */
   public String getRouting() {
      return routing;
   }
   /**
    * 路由
    *
    * @see CorrelationObjectLink
    */
   public void setRouting(String routing) throws wt.util.WTPropertyVetoException {
      routingValidate(routing);
      this.routing = routing;
   }
   void routingValidate(String routing) throws wt.util.WTPropertyVetoException {
      if (ROUTING_UPPER_LIMIT < 1) {
         try { ROUTING_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("routing").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ROUTING_UPPER_LIMIT = 200; }
      }
      if (routing != null && !wt.fc.PersistenceHelper.checkStoredLength(routing.toString(), ROUTING_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "routing"), String.valueOf(Math.min(ROUTING_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "routing", this.routing, routing));
   }

   /**
    * 审批意见
    *
    * @see CorrelationObjectLink
    */
   public static final String APPROVAL_OPINION = "approvalOpinion";
   static int APPROVAL_OPINION_UPPER_LIMIT = -1;
   String approvalOpinion;
   /**
    * 审批意见
    *
    * @see CorrelationObjectLink
    */
   public String getApprovalOpinion() {
      return approvalOpinion;
   }
   /**
    * 审批意见
    *
    * @see CorrelationObjectLink
    */
   public void setApprovalOpinion(String approvalOpinion) throws wt.util.WTPropertyVetoException {
      approvalOpinionValidate(approvalOpinion);
      this.approvalOpinion = approvalOpinion;
   }
   void approvalOpinionValidate(String approvalOpinion) throws wt.util.WTPropertyVetoException {
      if (APPROVAL_OPINION_UPPER_LIMIT < 1) {
         try { APPROVAL_OPINION_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("approvalOpinion").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { APPROVAL_OPINION_UPPER_LIMIT = 200; }
      }
      if (approvalOpinion != null && !wt.fc.PersistenceHelper.checkStoredLength(approvalOpinion.toString(), APPROVAL_OPINION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "approvalOpinion"), String.valueOf(Math.min(APPROVAL_OPINION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "approvalOpinion", this.approvalOpinion, approvalOpinion));
   }

   /**
    * 备注（驳回必填）
    *
    * @see CorrelationObjectLink
    */
   public static final String REMARK = "remark";
   static int REMARK_UPPER_LIMIT = -1;
   String remark;
   /**
    * 备注（驳回必填）
    *
    * @see CorrelationObjectLink
    */
   public String getRemark() {
      return remark;
   }
   /**
    * 备注（驳回必填）
    *
    * @see CorrelationObjectLink
    */
   public void setRemark(String remark) throws wt.util.WTPropertyVetoException {
      remarkValidate(remark);
      this.remark = remark;
   }
   void remarkValidate(String remark) throws wt.util.WTPropertyVetoException {
      if (REMARK_UPPER_LIMIT < 1) {
         try { REMARK_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("remark").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { REMARK_UPPER_LIMIT = 2000; }
      }
      if (remark != null && !wt.fc.PersistenceHelper.checkStoredLength(remark.toString(), REMARK_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "remark"), String.valueOf(Math.min(REMARK_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "remark", this.remark, remark));
   }

   /**
    * 收集对象
    *
    * @see CorrelationObjectLink
    */
   public static final String COLLECTION = "collection";
   static int COLLECTION_UPPER_LIMIT = -1;
   String collection;
   /**
    * 收集对象
    *
    * @see CorrelationObjectLink
    */
   public String getCollection() {
      return collection;
   }
   /**
    * 收集对象
    *
    * @see CorrelationObjectLink
    */
   public void setCollection(String collection) throws wt.util.WTPropertyVetoException {
      collectionValidate(collection);
      this.collection = collection;
   }
   void collectionValidate(String collection) throws wt.util.WTPropertyVetoException {
      if (COLLECTION_UPPER_LIMIT < 1) {
         try { COLLECTION_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("collection").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { COLLECTION_UPPER_LIMIT = 2000; }
      }
      if (collection != null && !wt.fc.PersistenceHelper.checkStoredLength(collection.toString(), COLLECTION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "collection"), String.valueOf(Math.min(COLLECTION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "collection", this.collection, collection));
   }

   /**
    * 处理方式
    *
    * @see CorrelationObjectLink
    */
   public static final String TREATMENT = "treatment";
   static int TREATMENT_UPPER_LIMIT = -1;
   String treatment;
   /**
    * 处理方式
    *
    * @see CorrelationObjectLink
    */
   public String getTreatment() {
      return treatment;
   }
   /**
    * 处理方式
    *
    * @see CorrelationObjectLink
    */
   public void setTreatment(String treatment) throws wt.util.WTPropertyVetoException {
      treatmentValidate(treatment);
      this.treatment = treatment;
   }
   void treatmentValidate(String treatment) throws wt.util.WTPropertyVetoException {
      if (TREATMENT_UPPER_LIMIT < 1) {
         try { TREATMENT_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("treatment").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { TREATMENT_UPPER_LIMIT = 2000; }
      }
      if (treatment != null && !wt.fc.PersistenceHelper.checkStoredLength(treatment.toString(), TREATMENT_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "treatment"), String.valueOf(Math.min(TREATMENT_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "treatment", this.treatment, treatment));
   }

   /**
    * @see CorrelationObjectLink
    */
   public static final String CHANGE_ORDER2_ROLE = "changeOrder2";
   /**
    * @see CorrelationObjectLink
    */
   public wt.change2.WTChangeOrder2 getChangeOrder2() {
      return (wt.change2.WTChangeOrder2) getRoleAObject();
   }
   /**
    * @see CorrelationObjectLink
    */
   public void setChangeOrder2(wt.change2.WTChangeOrder2 the_changeOrder2) throws wt.util.WTPropertyVetoException {
      setRoleAObject((wt.fc.Persistable) the_changeOrder2);
   }

   /**
    * @see CorrelationObjectLink
    */
   public static final String PERSISTABLE_ROLE = "persistable";
   /**
    * @see CorrelationObjectLink
    */
   public wt.fc.Persistable getPersistable() {
      return (wt.fc.Persistable) getRoleBObject();
   }
   /**
    * @see CorrelationObjectLink
    */
   public void setPersistable(wt.fc.Persistable the_persistable) throws wt.util.WTPropertyVetoException {
      setRoleBObject((wt.fc.Persistable) the_persistable);
   }

   public String getConceptualClassname() {
      return CLASSNAME;
   }

   public wt.introspection.ClassInfo getClassInfo() throws wt.introspection.WTIntrospectionException {
      return wt.introspection.WTIntrospector.getClassInfo(getConceptualClassname());
   }

   public String getType() {
      try { return getClassInfo().getDisplayName(); }
      catch (wt.introspection.WTIntrospectionException wte) { return wt.util.WTStringUtilities.tail(getConceptualClassname(), '.'); }
   }

   public static final long EXTERNALIZATION_VERSION_UID = -4039924139835728706L;

   public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
      output.writeLong( EXTERNALIZATION_VERSION_UID );

      super.writeExternal( output );

      output.writeObject( aadDescription );
      output.writeObject( approvalOpinion );
      output.writeObject( collection );
      output.writeObject( ecaIdentifier );
      output.writeObject( ecnBranchIdentifier );
      output.writeObject( linkType );
      output.writeObject( perBranchIdentifier );
      output.writeObject( remark );
      output.writeObject( routing );
      output.writeObject( treatment );
   }

   protected void super_writeExternal_CorrelationObjectLink(java.io.ObjectOutput output) throws java.io.IOException {
      super.writeExternal(output);
   }

   public void readExternal(java.io.ObjectInput input) throws java.io.IOException, ClassNotFoundException {
      long readSerialVersionUID = input.readLong();
      readVersion( (CorrelationObjectLink) this, input, readSerialVersionUID, false, false );
   }
   protected void super_readExternal_CorrelationObjectLink(java.io.ObjectInput input) throws java.io.IOException, ClassNotFoundException {
      super.readExternal(input);
   }

   public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.writeExternal( output );

      output.setString( "aadDescription", aadDescription );
      output.setString( "approvalOpinion", approvalOpinion );
      output.setString( "collection", collection );
      output.setString( "ecaIdentifier", ecaIdentifier );
      output.setString( "ecnBranchIdentifier", ecnBranchIdentifier );
      output.setString( "linkType", linkType );
      output.setString( "perBranchIdentifier", perBranchIdentifier );
      output.setString( "remark", remark );
      output.setString( "routing", routing );
      output.setString( "treatment", treatment );
   }

   public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.readExternal( input );

      aadDescription = input.getString( "aadDescription" );
      approvalOpinion = input.getString( "approvalOpinion" );
      collection = input.getString( "collection" );
      ecaIdentifier = input.getString( "ecaIdentifier" );
      ecnBranchIdentifier = input.getString( "ecnBranchIdentifier" );
      linkType = input.getString( "linkType" );
      perBranchIdentifier = input.getString( "perBranchIdentifier" );
      remark = input.getString( "remark" );
      routing = input.getString( "routing" );
      treatment = input.getString( "treatment" );
   }

   boolean readVersion_4039924139835728706L( java.io.ObjectInput input, long readSerialVersionUID, boolean superDone ) throws java.io.IOException, ClassNotFoundException {
      if ( !superDone )
         super.readExternal( input );

      aadDescription = (String) input.readObject();
      approvalOpinion = (String) input.readObject();
      collection = (String) input.readObject();
      ecaIdentifier = (String) input.readObject();
      ecnBranchIdentifier = (String) input.readObject();
      linkType = (String) input.readObject();
      perBranchIdentifier = (String) input.readObject();
      remark = (String) input.readObject();
      routing = (String) input.readObject();
      treatment = (String) input.readObject();
      return true;
   }

   protected boolean readVersion( CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, ClassNotFoundException {
      boolean success = true;

      if ( readSerialVersionUID == EXTERNALIZATION_VERSION_UID )
         return readVersion_4039924139835728706L( input, readSerialVersionUID, superDone );
      else
         success = readOldVersion( input, readSerialVersionUID, passThrough, superDone );

      if (input instanceof wt.pds.PDSObjectInput)
         wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

      return success;
   }
   protected boolean super_readVersion_CorrelationObjectLink( _CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, ClassNotFoundException {
      return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
   }

   boolean readOldVersion( java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, ClassNotFoundException {
      throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID="+readSerialVersionUID+" local class externalizationVersionUID="+EXTERNALIZATION_VERSION_UID);
   }
}
