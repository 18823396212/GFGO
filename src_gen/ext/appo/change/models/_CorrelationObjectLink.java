package ext.appo.change.models;

@SuppressWarnings({"cast", "rawtypes"})
public abstract class _CorrelationObjectLink extends wt.fc.ObjectToObjectLink implements java.io.Externalizable {
    static final long serialVersionUID = 1;

    static final String RESOURCE = "ext.appo.change.models.modelsResource";
    static final String CLASSNAME = CorrelationObjectLink.class.getName();

    /**
     * ECA对象VID
     *
     * @see CorrelationObjectLink
     */
    public static final String ECN_BRANCH_IDENTIFIER = "ecnBranchIdentifier";
    static int ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = -1;
    String ecnBranchIdentifier;

    /**
     * ECA对象VID
     *
     * @see CorrelationObjectLink
     */
    public String getEcnBranchIdentifier() {
        return ecnBranchIdentifier;
    }

    /**
     * ECA对象VID
     *
     * @see CorrelationObjectLink
     */
    public void setEcnBranchIdentifier(String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
        ecnBranchIdentifierValidate(ecnBranchIdentifier);
        this.ecnBranchIdentifier = ecnBranchIdentifier;
    }

    void ecnBranchIdentifierValidate(String ecnBranchIdentifier) throws wt.util.WTPropertyVetoException {
        if (ECN_BRANCH_IDENTIFIER_UPPER_LIMIT < 1) {
            try {
                ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("ecnBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT);
            } catch (wt.introspection.WTIntrospectionException e) {
                ECN_BRANCH_IDENTIFIER_UPPER_LIMIT = 200;
            }
        }
        if (ecnBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(ecnBranchIdentifier.toString(), ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
            throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT, new Object[]{new wt.introspection.PropertyDisplayName(CLASSNAME, "ecnBranchIdentifier"), String.valueOf(Math.min(ECN_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE / wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR))}, new java.beans.PropertyChangeEvent(this, "ecnBranchIdentifier", this.ecnBranchIdentifier, ecnBranchIdentifier));
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
            try {
                PER_BRANCH_IDENTIFIER_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("perBranchIdentifier").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT);
            } catch (wt.introspection.WTIntrospectionException e) {
                PER_BRANCH_IDENTIFIER_UPPER_LIMIT = 200;
            }
        }
        if (perBranchIdentifier != null && !wt.fc.PersistenceHelper.checkStoredLength(perBranchIdentifier.toString(), PER_BRANCH_IDENTIFIER_UPPER_LIMIT, true))
            throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT, new Object[]{new wt.introspection.PropertyDisplayName(CLASSNAME, "perBranchIdentifier"), String.valueOf(Math.min(PER_BRANCH_IDENTIFIER_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE / wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR))}, new java.beans.PropertyChangeEvent(this, "perBranchIdentifier", this.perBranchIdentifier, perBranchIdentifier));
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
            try {
                LINK_TYPE_UPPER_LIMIT = (Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("linkType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT);
            } catch (wt.introspection.WTIntrospectionException e) {
                LINK_TYPE_UPPER_LIMIT = 200;
            }
        }
        if (linkType != null && !wt.fc.PersistenceHelper.checkStoredLength(linkType.toString(), LINK_TYPE_UPPER_LIMIT, true))
            throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT, new Object[]{new wt.introspection.PropertyDisplayName(CLASSNAME, "linkType"), String.valueOf(Math.min(LINK_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE / wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR))}, new java.beans.PropertyChangeEvent(this, "linkType", this.linkType, linkType));
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
        try {
            return getClassInfo().getDisplayName();
        } catch (wt.introspection.WTIntrospectionException wte) {
            return wt.util.WTStringUtilities.tail(getConceptualClassname(), '.');
        }
    }

    public static final long EXTERNALIZATION_VERSION_UID = 8131924819007037915L;

    public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
        output.writeLong(EXTERNALIZATION_VERSION_UID);

        super.writeExternal(output);

        output.writeObject(ecnBranchIdentifier);
        output.writeObject(linkType);
        output.writeObject(perBranchIdentifier);
    }

    protected void super_writeExternal_CorrelationObjectLink(java.io.ObjectOutput output) throws java.io.IOException {
        super.writeExternal(output);
    }

    public void readExternal(java.io.ObjectInput input) throws java.io.IOException, ClassNotFoundException {
        long readSerialVersionUID = input.readLong();
        readVersion((CorrelationObjectLink) this, input, readSerialVersionUID, false, false);
    }

    protected void super_readExternal_CorrelationObjectLink(java.io.ObjectInput input) throws java.io.IOException, ClassNotFoundException {
        super.readExternal(input);
    }

    public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
        super.writeExternal(output);

        output.setString("ecnBranchIdentifier", ecnBranchIdentifier);
        output.setString("linkType", linkType);
        output.setString("perBranchIdentifier", perBranchIdentifier);
    }

    public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
        super.readExternal(input);

        ecnBranchIdentifier = input.getString("ecnBranchIdentifier");
        linkType = input.getString("linkType");
        perBranchIdentifier = input.getString("perBranchIdentifier");
    }

    boolean readVersion8131924819007037915L(java.io.ObjectInput input, long readSerialVersionUID, boolean superDone) throws java.io.IOException, ClassNotFoundException {
        if (!superDone) super.readExternal(input);

        ecnBranchIdentifier = (String) input.readObject();
        linkType = (String) input.readObject();
        perBranchIdentifier = (String) input.readObject();
        return true;
    }

    protected boolean readVersion(CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone) throws java.io.IOException, ClassNotFoundException {
        boolean success = true;

        if (readSerialVersionUID == EXTERNALIZATION_VERSION_UID)
            return readVersion8131924819007037915L(input, readSerialVersionUID, superDone);
        else success = readOldVersion(input, readSerialVersionUID, passThrough, superDone);

        if (input instanceof wt.pds.PDSObjectInput) wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

        return success;
    }

    protected boolean super_readVersion_CorrelationObjectLink(_CorrelationObjectLink thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone) throws java.io.IOException, ClassNotFoundException {
        return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
    }

    boolean readOldVersion(java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID=" + readSerialVersionUID + " local class externalizationVersionUID=" + EXTERNALIZATION_VERSION_UID);
    }
}
