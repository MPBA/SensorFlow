package eu.fbk.mpba.sensorsflows.plugins.outputs.skilo;
@SuppressWarnings("ALL")
public final class SensorsProtobuffer {
    private SensorsProtobuffer() {}
    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
    }
    public interface SensorDataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:SensorData)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>required int32 id = 1;</code>
         */
        boolean hasId();
        /**
         * <code>required int32 id = 1;</code>
         */
        int getId();

        /**
         * <code>required int32 sensor_id_fk = 2;</code>
         */
        boolean hasSensorIdFk();
        /**
         * <code>required int32 sensor_id_fk = 2;</code>
         */
        int getSensorIdFk();

        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        java.util.List<java.lang.Double> getValueList();
        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        int getValueCount();
        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        double getValue(int index);

        /**
         * <code>repeated string text = 4;</code>
         */
        com.google.protobuf.ProtocolStringList
        getTextList();
        /**
         * <code>repeated string text = 4;</code>
         */
        int getTextCount();
        /**
         * <code>repeated string text = 4;</code>
         */
        java.lang.String getText(int index);
        /**
         * <code>repeated string text = 4;</code>
         */
        com.google.protobuf.ByteString
        getTextBytes(int index);

        /**
         * <code>required double timestamp = 5;</code>
         */
        boolean hasTimestamp();
        /**
         * <code>required double timestamp = 5;</code>
         */
        double getTimestamp();

        /**
         * <code>optional int64 packet_counter = 6;</code>
         */
        boolean hasPacketCounter();
        /**
         * <code>optional int64 packet_counter = 6;</code>
         */
        long getPacketCounter();
    }
    /**
     * Protobuf type {@code SensorData}
     */
    public static final class SensorData extends
            com.google.protobuf.GeneratedMessage implements
            // @@protoc_insertion_point(message_implements:SensorData)
            SensorDataOrBuilder {
        // Use SensorData.newBuilder() to construct.
        private SensorData(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }
        private SensorData(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

        private static final SensorData defaultInstance;
        public static SensorData getDefaultInstance() {
            return defaultInstance;
        }

        public SensorData getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;
        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }
        private SensorData(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 8: {
                            bitField0_ |= 0x00000001;
                            id_ = input.readInt32();
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000002;
                            sensorIdFk_ = input.readInt32();
                            break;
                        }
                        case 25: {
                            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
                                value_ = new java.util.ArrayList<java.lang.Double>();
                                mutable_bitField0_ |= 0x00000004;
                            }
                            value_.add(input.readDouble());
                            break;
                        }
                        case 26: {
                            int length = input.readRawVarint32();
                            int limit = input.pushLimit(length);
                            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004) && input.getBytesUntilLimit() > 0) {
                                value_ = new java.util.ArrayList<java.lang.Double>();
                                mutable_bitField0_ |= 0x00000004;
                            }
                            while (input.getBytesUntilLimit() > 0) {
                                value_.add(input.readDouble());
                            }
                            input.popLimit(limit);
                            break;
                        }
                        case 34: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                                text_ = new com.google.protobuf.LazyStringArrayList();
                                mutable_bitField0_ |= 0x00000008;
                            }
                            text_.add(bs);
                            break;
                        }
                        case 41: {
                            bitField0_ |= 0x00000004;
                            timestamp_ = input.readDouble();
                            break;
                        }
                        case 48: {
                            bitField0_ |= 0x00000008;
                            packetCounter_ = input.readInt64();
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
                    value_ = java.util.Collections.unmodifiableList(value_);
                }
                if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                    text_ = text_.getUnmodifiableView();
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }
        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return SensorsProtobuffer.internal_static_SensorData_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return SensorsProtobuffer.internal_static_SensorData_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            SensorsProtobuffer.SensorData.class, SensorsProtobuffer.SensorData.Builder.class);
        }

        public static com.google.protobuf.Parser<SensorData> PARSER =
                new com.google.protobuf.AbstractParser<SensorData>() {
                    public SensorData parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new SensorData(input, extensionRegistry);
                    }
                };

        @java.lang.Override
        public com.google.protobuf.Parser<SensorData> getParserForType() {
            return PARSER;
        }

        private int bitField0_;
        public static final int ID_FIELD_NUMBER = 1;
        private int id_;
        /**
         * <code>required int32 id = 1;</code>
         */
        public boolean hasId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>required int32 id = 1;</code>
         */
        public int getId() {
            return id_;
        }

        public static final int SENSOR_ID_FK_FIELD_NUMBER = 2;
        private int sensorIdFk_;
        /**
         * <code>required int32 sensor_id_fk = 2;</code>
         */
        public boolean hasSensorIdFk() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>required int32 sensor_id_fk = 2;</code>
         */
        public int getSensorIdFk() {
            return sensorIdFk_;
        }

        public static final int VALUE_FIELD_NUMBER = 3;
        private java.util.List<java.lang.Double> value_;
        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        public java.util.List<java.lang.Double>
        getValueList() {
            return value_;
        }
        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        public int getValueCount() {
            return value_.size();
        }
        /**
         * <code>repeated double value = 3 [packed = true];</code>
         */
        public double getValue(int index) {
            return value_.get(index);
        }
        private int valueMemoizedSerializedSize = -1;

        public static final int TEXT_FIELD_NUMBER = 4;
        private com.google.protobuf.LazyStringList text_;
        /**
         * <code>repeated string text = 4;</code>
         */
        public com.google.protobuf.ProtocolStringList
        getTextList() {
            return text_;
        }
        /**
         * <code>repeated string text = 4;</code>
         */
        public int getTextCount() {
            return text_.size();
        }
        /**
         * <code>repeated string text = 4;</code>
         */
        public java.lang.String getText(int index) {
            return text_.get(index);
        }
        /**
         * <code>repeated string text = 4;</code>
         */
        public com.google.protobuf.ByteString
        getTextBytes(int index) {
            return text_.getByteString(index);
        }

        public static final int TIMESTAMP_FIELD_NUMBER = 5;
        private double timestamp_;
        /**
         * <code>required double timestamp = 5;</code>
         */
        public boolean hasTimestamp() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }
        /**
         * <code>required double timestamp = 5;</code>
         */
        public double getTimestamp() {
            return timestamp_;
        }

        public static final int PACKET_COUNTER_FIELD_NUMBER = 6;
        private long packetCounter_;
        /**
         * <code>optional int64 packet_counter = 6;</code>
         */
        public boolean hasPacketCounter() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }
        /**
         * <code>optional int64 packet_counter = 6;</code>
         */
        public long getPacketCounter() {
            return packetCounter_;
        }

        private void initFields() {
            id_ = 0;
            sensorIdFk_ = 0;
            value_ = java.util.Collections.emptyList();
            text_ = com.google.protobuf.LazyStringArrayList.EMPTY;
            timestamp_ = 0D;
            packetCounter_ = 0L;
        }
        private byte memoizedIsInitialized = -1;
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            if (!hasId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasSensorIdFk()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTimestamp()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeInt32(1, id_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt32(2, sensorIdFk_);
            }
            if (getValueList().size() > 0) {
                output.writeRawVarint32(26);
                output.writeRawVarint32(valueMemoizedSerializedSize);
            }
            for (int i = 0; i < value_.size(); i++) {
                output.writeDoubleNoTag(value_.get(i));
            }
            for (int i = 0; i < text_.size(); i++) {
                output.writeBytes(4, text_.getByteString(i));
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeDouble(5, timestamp_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeInt64(6, packetCounter_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;
        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(1, id_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(2, sensorIdFk_);
            }
            {
                int dataSize = 0;
                dataSize = 8 * getValueList().size();
                size += dataSize;
                if (!getValueList().isEmpty()) {
                    size += 1;
                    size += com.google.protobuf.CodedOutputStream
                            .computeInt32SizeNoTag(dataSize);
                }
                valueMemoizedSerializedSize = dataSize;
            }
            {
                int dataSize = 0;
                for (int i = 0; i < text_.size(); i++) {
                    dataSize += com.google.protobuf.CodedOutputStream
                            .computeBytesSizeNoTag(text_.getByteString(i));
                }
                size += dataSize;
                size += 1 * getTextList().size();
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeDoubleSize(5, timestamp_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(6, packetCounter_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;
        @java.lang.Override
        protected java.lang.Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public static SensorsProtobuffer.SensorData parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.SensorData parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorData parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.SensorData parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorData parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.SensorData parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorData parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }
        public static SensorsProtobuffer.SensorData parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorData parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.SensorData parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() { return Builder.create(); }
        public Builder newBuilderForType() { return newBuilder(); }
        public static Builder newBuilder(SensorsProtobuffer.SensorData prototype) {
            return newBuilder().mergeFrom(prototype);
        }
        public Builder toBuilder() { return newBuilder(this); }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }
        /**
         * Protobuf type {@code SensorData}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:SensorData)
                SensorsProtobuffer.SensorDataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return SensorsProtobuffer.internal_static_SensorData_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return SensorsProtobuffer.internal_static_SensorData_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                SensorsProtobuffer.SensorData.class, SensorsProtobuffer.SensorData.Builder.class);
            }

            // Construct using Track.SensorData.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }
            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }
            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                id_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                sensorIdFk_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                value_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000004);
                text_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000008);
                timestamp_ = 0D;
                bitField0_ = (bitField0_ & ~0x00000010);
                packetCounter_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000020);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return SensorsProtobuffer.internal_static_SensorData_descriptor;
            }

            public SensorsProtobuffer.SensorData getDefaultInstanceForType() {
                return SensorsProtobuffer.SensorData.getDefaultInstance();
            }

            public SensorsProtobuffer.SensorData build() {
                SensorsProtobuffer.SensorData result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public SensorsProtobuffer.SensorData buildPartial() {
                SensorsProtobuffer.SensorData result = new SensorsProtobuffer.SensorData(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.id_ = id_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.sensorIdFk_ = sensorIdFk_;
                if (((bitField0_ & 0x00000004) == 0x00000004)) {
                    value_ = java.util.Collections.unmodifiableList(value_);
                    bitField0_ = (bitField0_ & ~0x00000004);
                }
                result.value_ = value_;
                if (((bitField0_ & 0x00000008) == 0x00000008)) {
                    text_ = text_.getUnmodifiableView();
                    bitField0_ = (bitField0_ & ~0x00000008);
                }
                result.text_ = text_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.timestamp_ = timestamp_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.packetCounter_ = packetCounter_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof SensorsProtobuffer.SensorData) {
                    return mergeFrom((SensorsProtobuffer.SensorData)other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(SensorsProtobuffer.SensorData other) {
                if (other == SensorsProtobuffer.SensorData.getDefaultInstance()) return this;
                if (other.hasId()) {
                    setId(other.getId());
                }
                if (other.hasSensorIdFk()) {
                    setSensorIdFk(other.getSensorIdFk());
                }
                if (!other.value_.isEmpty()) {
                    if (value_.isEmpty()) {
                        value_ = other.value_;
                        bitField0_ = (bitField0_ & ~0x00000004);
                    } else {
                        ensureValueIsMutable();
                        value_.addAll(other.value_);
                    }
                    onChanged();
                }
                if (!other.text_.isEmpty()) {
                    if (text_.isEmpty()) {
                        text_ = other.text_;
                        bitField0_ = (bitField0_ & ~0x00000008);
                    } else {
                        ensureTextIsMutable();
                        text_.addAll(other.text_);
                    }
                    onChanged();
                }
                if (other.hasTimestamp()) {
                    setTimestamp(other.getTimestamp());
                }
                if (other.hasPacketCounter()) {
                    setPacketCounter(other.getPacketCounter());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasId()) {

                    return false;
                }
                if (!hasSensorIdFk()) {

                    return false;
                }
                if (!hasTimestamp()) {

                    return false;
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                SensorsProtobuffer.SensorData parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (SensorsProtobuffer.SensorData) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
            private int bitField0_;

            private int id_ ;
            /**
             * <code>required int32 id = 1;</code>
             */
            public boolean hasId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }
            /**
             * <code>required int32 id = 1;</code>
             */
            public int getId() {
                return id_;
            }
            /**
             * <code>required int32 id = 1;</code>
             */
            public Builder setId(int value) {
                bitField0_ |= 0x00000001;
                id_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required int32 id = 1;</code>
             */
            public Builder clearId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                id_ = 0;
                onChanged();
                return this;
            }

            private int sensorIdFk_ ;
            /**
             * <code>required int32 sensor_id_fk = 2;</code>
             */
            public boolean hasSensorIdFk() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }
            /**
             * <code>required int32 sensor_id_fk = 2;</code>
             */
            public int getSensorIdFk() {
                return sensorIdFk_;
            }
            /**
             * <code>required int32 sensor_id_fk = 2;</code>
             */
            public Builder setSensorIdFk(int value) {
                bitField0_ |= 0x00000002;
                sensorIdFk_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required int32 sensor_id_fk = 2;</code>
             */
            public Builder clearSensorIdFk() {
                bitField0_ = (bitField0_ & ~0x00000002);
                sensorIdFk_ = 0;
                onChanged();
                return this;
            }

            private java.util.List<java.lang.Double> value_ = java.util.Collections.emptyList();
            private void ensureValueIsMutable() {
                if (!((bitField0_ & 0x00000004) == 0x00000004)) {
                    value_ = new java.util.ArrayList<java.lang.Double>(value_);
                    bitField0_ |= 0x00000004;
                }
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public java.util.List<java.lang.Double>
            getValueList() {
                return java.util.Collections.unmodifiableList(value_);
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public int getValueCount() {
                return value_.size();
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public double getValue(int index) {
                return value_.get(index);
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public Builder setValue(
                    int index, double value) {
                ensureValueIsMutable();
                value_.set(index, value);
                onChanged();
                return this;
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public Builder addValue(double value) {
                ensureValueIsMutable();
                value_.add(value);
                onChanged();
                return this;
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public Builder addAllValue(
                    java.lang.Iterable<? extends java.lang.Double> values) {
                ensureValueIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, value_);
                onChanged();
                return this;
            }
            /**
             * <code>repeated double value = 3 [packed = true];</code>
             */
            public Builder clearValue() {
                value_ = java.util.Collections.emptyList();
                bitField0_ = (bitField0_ & ~0x00000004);
                onChanged();
                return this;
            }

            private com.google.protobuf.LazyStringList text_ = com.google.protobuf.LazyStringArrayList.EMPTY;
            private void ensureTextIsMutable() {
                if (!((bitField0_ & 0x00000008) == 0x00000008)) {
                    text_ = new com.google.protobuf.LazyStringArrayList(text_);
                    bitField0_ |= 0x00000008;
                }
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public com.google.protobuf.ProtocolStringList
            getTextList() {
                return text_.getUnmodifiableView();
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public int getTextCount() {
                return text_.size();
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public java.lang.String getText(int index) {
                return text_.get(index);
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public com.google.protobuf.ByteString
            getTextBytes(int index) {
                return text_.getByteString(index);
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public Builder setText(
                    int index, java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTextIsMutable();
                text_.set(index, value);
                onChanged();
                return this;
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public Builder addText(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTextIsMutable();
                text_.add(value);
                onChanged();
                return this;
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public Builder addAllText(
                    java.lang.Iterable<java.lang.String> values) {
                ensureTextIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, text_);
                onChanged();
                return this;
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public Builder clearText() {
                text_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000008);
                onChanged();
                return this;
            }
            /**
             * <code>repeated string text = 4;</code>
             */
            public Builder addTextBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureTextIsMutable();
                text_.add(value);
                onChanged();
                return this;
            }

            private double timestamp_ ;
            /**
             * <code>required double timestamp = 5;</code>
             */
            public boolean hasTimestamp() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }
            /**
             * <code>required double timestamp = 5;</code>
             */
            public double getTimestamp() {
                return timestamp_;
            }
            /**
             * <code>required double timestamp = 5;</code>
             */
            public Builder setTimestamp(double value) {
                bitField0_ |= 0x00000010;
                timestamp_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required double timestamp = 5;</code>
             */
            public Builder clearTimestamp() {
                bitField0_ = (bitField0_ & ~0x00000010);
                timestamp_ = 0D;
                onChanged();
                return this;
            }

            private long packetCounter_ ;
            /**
             * <code>optional int64 packet_counter = 6;</code>
             */
            public boolean hasPacketCounter() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }
            /**
             * <code>optional int64 packet_counter = 6;</code>
             */
            public long getPacketCounter() {
                return packetCounter_;
            }
            /**
             * <code>optional int64 packet_counter = 6;</code>
             */
            public Builder setPacketCounter(long value) {
                bitField0_ |= 0x00000020;
                packetCounter_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional int64 packet_counter = 6;</code>
             */
            public Builder clearPacketCounter() {
                bitField0_ = (bitField0_ & ~0x00000020);
                packetCounter_ = 0L;
                onChanged();
                return this;
            }

            // @@protoc_insertion_point(builder_scope:SensorData)
        }

        static {
            defaultInstance = new SensorData(true);
            defaultInstance.initFields();
        }

        // @@protoc_insertion_point(class_scope:SensorData)
    }

    public interface SensorInfoOrBuilder extends
            // @@protoc_insertion_point(interface_extends:SensorInfo)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>required int32 sensor_id = 1;</code>
         */
        boolean hasSensorId();
        /**
         * <code>required int32 sensor_id = 1;</code>
         */
        int getSensorId();

        /**
         * <code>required string desc = 2;</code>
         */
        boolean hasDesc();
        /**
         * <code>required string desc = 2;</code>
         */
        java.lang.String getDesc();
        /**
         * <code>required string desc = 2;</code>
         */
        com.google.protobuf.ByteString
        getDescBytes();

        /**
         * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
         */
        boolean hasType();
        /**
         * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
         */
        SensorsProtobuffer.SensorInfo.TYPESENSOR getType();

        /**
         * <code>optional string meta = 4;</code>
         */
        boolean hasMeta();
        /**
         * <code>optional string meta = 4;</code>
         */
        java.lang.String getMeta();
        /**
         * <code>optional string meta = 4;</code>
         */
        com.google.protobuf.ByteString
        getMetaBytes();
    }
    /**
     * Protobuf type {@code SensorInfo}
     */
    public static final class SensorInfo extends
            com.google.protobuf.GeneratedMessage implements
            // @@protoc_insertion_point(message_implements:SensorInfo)
            SensorInfoOrBuilder {
        // Use SensorInfo.newBuilder() to construct.
        private SensorInfo(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }
        private SensorInfo(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

        private static final SensorInfo defaultInstance;
        public static SensorInfo getDefaultInstance() {
            return defaultInstance;
        }

        public SensorInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;
        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }
        private SensorInfo(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 8: {
                            bitField0_ |= 0x00000001;
                            sensorId_ = input.readInt32();
                            break;
                        }
                        case 18: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000002;
                            desc_ = bs;
                            break;
                        }
                        case 24: {
                            int rawValue = input.readEnum();
                            SensorsProtobuffer.SensorInfo.TYPESENSOR value = SensorsProtobuffer.SensorInfo.TYPESENSOR.valueOf(rawValue);
                            if (value == null) {
                                unknownFields.mergeVarintField(3, rawValue);
                            } else {
                                bitField0_ |= 0x00000004;
                                type_ = value;
                            }
                            break;
                        }
                        case 34: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000008;
                            meta_ = bs;
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }
        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return SensorsProtobuffer.internal_static_SensorInfo_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return SensorsProtobuffer.internal_static_SensorInfo_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            SensorsProtobuffer.SensorInfo.class, SensorsProtobuffer.SensorInfo.Builder.class);
        }

        public static com.google.protobuf.Parser<SensorInfo> PARSER =
                new com.google.protobuf.AbstractParser<SensorInfo>() {
                    public SensorInfo parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new SensorInfo(input, extensionRegistry);
                    }
                };

        @java.lang.Override
        public com.google.protobuf.Parser<SensorInfo> getParserForType() {
            return PARSER;
        }

        /**
         * Protobuf enum {@code SensorInfo.TYPESENSOR}
         */
        public enum TYPESENSOR
                implements com.google.protobuf.ProtocolMessageEnum {
            /**
             * <code>OTHER = 0;</code>
             */
            OTHER(0, 0),
            /**
             * <code>ACC = 1;</code>
             */
            ACC(1, 1),
            /**
             * <code>GYRO = 2;</code>
             */
            GYRO(2, 2),
            /**
             * <code>MAGNE = 3;</code>
             */
            MAGNE(3, 3),
            /**
             * <code>BVP = 4;</code>
             */
            BVP(4, 4),
            /**
             * <code>GPS = 5;</code>
             */
            GPS(5, 5),
            /**
             * <code>BPM = 6;</code>
             */
            BPM(6, 6),
            /**
             * <code>GSR = 7;</code>
             */
            GSR(7, 7),
            /**
             * <code>BATTERY = 8;</code>
             */
            BATTERY(8, 8),
            /**
             * <code>TEMP = 9;</code>
             */
            TEMP(9, 9),
            /**
             * <code>LUX = 10;</code>
             */
            LUX(10, 10),
            /**
             * <code>IBI = 11;</code>
             */
            IBI(11, 11),
            /**
             * <code>WIFI = 12;</code>
             */
            WIFI(12, 12),
            /**
             * <code>GSM = 13;</code>
             */
            GSM(13, 13),
            /**
             * <code>MARKER = 14;</code>
             */
            MARKER(14, 14),
            /**
             * <code>EMG = 15;</code>
             */
            EMG(15, 15),
            /**
             * <code>QUAT = 16;</code>
             */
            QUAT(16, 16),
            /**
             * <code>EEG = 17;</code>
             */
            EEG(17, 17),
            /**
             * <code>ECG = 18;</code>
             */
            ECG(18, 18),
            ;

            /**
             * <code>OTHER = 0;</code>
             */
            public static final int OTHER_VALUE = 0;
            /**
             * <code>ACC = 1;</code>
             */
            public static final int ACC_VALUE = 1;
            /**
             * <code>GYRO = 2;</code>
             */
            public static final int GYRO_VALUE = 2;
            /**
             * <code>MAGNE = 3;</code>
             */
            public static final int MAGNE_VALUE = 3;
            /**
             * <code>BVP = 4;</code>
             */
            public static final int BVP_VALUE = 4;
            /**
             * <code>GPS = 5;</code>
             */
            public static final int GPS_VALUE = 5;
            /**
             * <code>BPM = 6;</code>
             */
            public static final int BPM_VALUE = 6;
            /**
             * <code>GSR = 7;</code>
             */
            public static final int GSR_VALUE = 7;
            /**
             * <code>BATTERY = 8;</code>
             */
            public static final int BATTERY_VALUE = 8;
            /**
             * <code>TEMP = 9;</code>
             */
            public static final int TEMP_VALUE = 9;
            /**
             * <code>LUX = 10;</code>
             */
            public static final int LUX_VALUE = 10;
            /**
             * <code>IBI = 11;</code>
             */
            public static final int IBI_VALUE = 11;
            /**
             * <code>WIFI = 12;</code>
             */
            public static final int WIFI_VALUE = 12;
            /**
             * <code>GSM = 13;</code>
             */
            public static final int GSM_VALUE = 13;
            /**
             * <code>MARKER = 14;</code>
             */
            public static final int MARKER_VALUE = 14;
            /**
             * <code>EMG = 15;</code>
             */
            public static final int EMG_VALUE = 15;
            /**
             * <code>QUAT = 16;</code>
             */
            public static final int QUAT_VALUE = 16;
            /**
             * <code>EEG = 17;</code>
             */
            public static final int EEG_VALUE = 17;
            /**
             * <code>ECG = 18;</code>
             */
            public static final int ECG_VALUE = 18;


            public final int getNumber() { return value; }

            public static TYPESENSOR valueOf(int value) {
                switch (value) {
                    case 0: return OTHER;
                    case 1: return ACC;
                    case 2: return GYRO;
                    case 3: return MAGNE;
                    case 4: return BVP;
                    case 5: return GPS;
                    case 6: return BPM;
                    case 7: return GSR;
                    case 8: return BATTERY;
                    case 9: return TEMP;
                    case 10: return LUX;
                    case 11: return IBI;
                    case 12: return WIFI;
                    case 13: return GSM;
                    case 14: return MARKER;
                    case 15: return EMG;
                    case 16: return QUAT;
                    case 17: return EEG;
                    case 18: return ECG;
                    default: return null;
                }
            }

            public static com.google.protobuf.Internal.EnumLiteMap<TYPESENSOR>
            internalGetValueMap() {
                return internalValueMap;
            }
            private static com.google.protobuf.Internal.EnumLiteMap<TYPESENSOR>
                    internalValueMap =
                    new com.google.protobuf.Internal.EnumLiteMap<TYPESENSOR>() {
                        public TYPESENSOR findValueByNumber(int number) {
                            return TYPESENSOR.valueOf(number);
                        }
                    };

            public final com.google.protobuf.Descriptors.EnumValueDescriptor
            getValueDescriptor() {
                return getDescriptor().getValues().get(index);
            }
            public final com.google.protobuf.Descriptors.EnumDescriptor
            getDescriptorForType() {
                return getDescriptor();
            }
            public static final com.google.protobuf.Descriptors.EnumDescriptor
            getDescriptor() {
                return SensorsProtobuffer.SensorInfo.getDescriptor().getEnumTypes().get(0);
            }

            private static final TYPESENSOR[] VALUES = values();

            public static TYPESENSOR valueOf(
                    com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
                if (desc.getType() != getDescriptor()) {
                    throw new java.lang.IllegalArgumentException(
                            "EnumValueDescriptor is not for this type.");
                }
                return VALUES[desc.getIndex()];
            }

            private final int index;
            private final int value;

            private TYPESENSOR(int index, int value) {
                this.index = index;
                this.value = value;
            }

            // @@protoc_insertion_point(enum_scope:SensorInfo.TYPESENSOR)
        }

        private int bitField0_;
        public static final int SENSOR_ID_FIELD_NUMBER = 1;
        private int sensorId_;
        /**
         * <code>required int32 sensor_id = 1;</code>
         */
        public boolean hasSensorId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>required int32 sensor_id = 1;</code>
         */
        public int getSensorId() {
            return sensorId_;
        }

        public static final int DESC_FIELD_NUMBER = 2;
        private java.lang.Object desc_;
        /**
         * <code>required string desc = 2;</code>
         */
        public boolean hasDesc() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>required string desc = 2;</code>
         */
        public java.lang.String getDesc() {
            java.lang.Object ref = desc_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    desc_ = s;
                }
                return s;
            }
        }
        /**
         * <code>required string desc = 2;</code>
         */
        public com.google.protobuf.ByteString
        getDescBytes() {
            java.lang.Object ref = desc_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                desc_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int TYPE_FIELD_NUMBER = 3;
        private SensorsProtobuffer.SensorInfo.TYPESENSOR type_;
        /**
         * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
         */
        public boolean hasType() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }
        /**
         * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
         */
        public SensorsProtobuffer.SensorInfo.TYPESENSOR getType() {
            return type_;
        }

        public static final int META_FIELD_NUMBER = 4;
        private java.lang.Object meta_;
        /**
         * <code>optional string meta = 4;</code>
         */
        public boolean hasMeta() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }
        /**
         * <code>optional string meta = 4;</code>
         */
        public java.lang.String getMeta() {
            java.lang.Object ref = meta_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    meta_ = s;
                }
                return s;
            }
        }
        /**
         * <code>optional string meta = 4;</code>
         */
        public com.google.protobuf.ByteString
        getMetaBytes() {
            java.lang.Object ref = meta_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                meta_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            sensorId_ = 0;
            desc_ = "";
            type_ = SensorsProtobuffer.SensorInfo.TYPESENSOR.OTHER;
            meta_ = "";
        }
        private byte memoizedIsInitialized = -1;
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            if (!hasSensorId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasDesc()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasType()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeInt32(1, sensorId_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getDescBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeEnum(3, type_.getNumber());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBytes(4, getMetaBytes());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;
        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(1, sensorId_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(2, getDescBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeEnumSize(3, type_.getNumber());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(4, getMetaBytes());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;
        @java.lang.Override
        protected java.lang.Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public static SensorsProtobuffer.SensorInfo parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorInfo parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }
        public static SensorsProtobuffer.SensorInfo parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.SensorInfo parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() { return Builder.create(); }
        public Builder newBuilderForType() { return newBuilder(); }
        public static Builder newBuilder(SensorsProtobuffer.SensorInfo prototype) {
            return newBuilder().mergeFrom(prototype);
        }
        public Builder toBuilder() { return newBuilder(this); }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }
        /**
         * Protobuf type {@code SensorInfo}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:SensorInfo)
                SensorsProtobuffer.SensorInfoOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return SensorsProtobuffer.internal_static_SensorInfo_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return SensorsProtobuffer.internal_static_SensorInfo_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                SensorsProtobuffer.SensorInfo.class, SensorsProtobuffer.SensorInfo.Builder.class);
            }

            // Construct using Track.SensorInfo.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }
            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }
            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                sensorId_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                desc_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                type_ = SensorsProtobuffer.SensorInfo.TYPESENSOR.OTHER;
                bitField0_ = (bitField0_ & ~0x00000004);
                meta_ = "";
                bitField0_ = (bitField0_ & ~0x00000008);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return SensorsProtobuffer.internal_static_SensorInfo_descriptor;
            }

            public SensorsProtobuffer.SensorInfo getDefaultInstanceForType() {
                return SensorsProtobuffer.SensorInfo.getDefaultInstance();
            }

            public SensorsProtobuffer.SensorInfo build() {
                SensorsProtobuffer.SensorInfo result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public SensorsProtobuffer.SensorInfo buildPartial() {
                SensorsProtobuffer.SensorInfo result = new SensorsProtobuffer.SensorInfo(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.sensorId_ = sensorId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.desc_ = desc_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.type_ = type_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.meta_ = meta_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof SensorsProtobuffer.SensorInfo) {
                    return mergeFrom((SensorsProtobuffer.SensorInfo)other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(SensorsProtobuffer.SensorInfo other) {
                if (other == SensorsProtobuffer.SensorInfo.getDefaultInstance()) return this;
                if (other.hasSensorId()) {
                    setSensorId(other.getSensorId());
                }
                if (other.hasDesc()) {
                    bitField0_ |= 0x00000002;
                    desc_ = other.desc_;
                    onChanged();
                }
                if (other.hasType()) {
                    setType(other.getType());
                }
                if (other.hasMeta()) {
                    bitField0_ |= 0x00000008;
                    meta_ = other.meta_;
                    onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasSensorId()) {

                    return false;
                }
                if (!hasDesc()) {

                    return false;
                }
                if (!hasType()) {

                    return false;
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                SensorsProtobuffer.SensorInfo parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (SensorsProtobuffer.SensorInfo) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
            private int bitField0_;

            private int sensorId_ ;
            /**
             * <code>required int32 sensor_id = 1;</code>
             */
            public boolean hasSensorId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }
            /**
             * <code>required int32 sensor_id = 1;</code>
             */
            public int getSensorId() {
                return sensorId_;
            }
            /**
             * <code>required int32 sensor_id = 1;</code>
             */
            public Builder setSensorId(int value) {
                bitField0_ |= 0x00000001;
                sensorId_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required int32 sensor_id = 1;</code>
             */
            public Builder clearSensorId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                sensorId_ = 0;
                onChanged();
                return this;
            }

            private java.lang.Object desc_ = "";
            /**
             * <code>required string desc = 2;</code>
             */
            public boolean hasDesc() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }
            /**
             * <code>required string desc = 2;</code>
             */
            public java.lang.String getDesc() {
                java.lang.Object ref = desc_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        desc_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>required string desc = 2;</code>
             */
            public com.google.protobuf.ByteString
            getDescBytes() {
                java.lang.Object ref = desc_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    desc_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>required string desc = 2;</code>
             */
            public Builder setDesc(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                desc_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required string desc = 2;</code>
             */
            public Builder clearDesc() {
                bitField0_ = (bitField0_ & ~0x00000002);
                desc_ = getDefaultInstance().getDesc();
                onChanged();
                return this;
            }
            /**
             * <code>required string desc = 2;</code>
             */
            public Builder setDescBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                desc_ = value;
                onChanged();
                return this;
            }

            private SensorsProtobuffer.SensorInfo.TYPESENSOR type_ = SensorsProtobuffer.SensorInfo.TYPESENSOR.OTHER;
            /**
             * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
             */
            public boolean hasType() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }
            /**
             * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
             */
            public SensorsProtobuffer.SensorInfo.TYPESENSOR getType() {
                return type_;
            }
            /**
             * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
             */
            public Builder setType(SensorsProtobuffer.SensorInfo.TYPESENSOR value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                type_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required .SensorInfo.TYPESENSOR type = 3;</code>
             */
            public Builder clearType() {
                bitField0_ = (bitField0_ & ~0x00000004);
                type_ = SensorsProtobuffer.SensorInfo.TYPESENSOR.OTHER;
                onChanged();
                return this;
            }

            private java.lang.Object meta_ = "";
            /**
             * <code>optional string meta = 4;</code>
             */
            public boolean hasMeta() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }
            /**
             * <code>optional string meta = 4;</code>
             */
            public java.lang.String getMeta() {
                java.lang.Object ref = meta_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        meta_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>optional string meta = 4;</code>
             */
            public com.google.protobuf.ByteString
            getMetaBytes() {
                java.lang.Object ref = meta_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    meta_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>optional string meta = 4;</code>
             */
            public Builder setMeta(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                meta_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional string meta = 4;</code>
             */
            public Builder clearMeta() {
                bitField0_ = (bitField0_ & ~0x00000008);
                meta_ = getDefaultInstance().getMeta();
                onChanged();
                return this;
            }
            /**
             * <code>optional string meta = 4;</code>
             */
            public Builder setMetaBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                meta_ = value;
                onChanged();
                return this;
            }

            // @@protoc_insertion_point(builder_scope:SensorInfo)
        }

        static {
            defaultInstance = new SensorInfo(true);
            defaultInstance.initFields();
        }

        // @@protoc_insertion_point(class_scope:SensorInfo)
    }

    public interface TrackSplitOrBuilder extends
            // @@protoc_insertion_point(interface_extends:TrackSplit)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>required string phone_id = 1;</code>
         */
        boolean hasPhoneId();
        /**
         * <code>required string phone_id = 1;</code>
         */
        java.lang.String getPhoneId();
        /**
         * <code>required string phone_id = 1;</code>
         */
        com.google.protobuf.ByteString
        getPhoneIdBytes();

        /**
         * <code>required string track_uid = 2;</code>
         */
        boolean hasTrackUid();
        /**
         * <code>required string track_uid = 2;</code>
         */
        java.lang.String getTrackUid();
        /**
         * <code>required string track_uid = 2;</code>
         */
        com.google.protobuf.ByteString
        getTrackUidBytes();

        /**
         * <code>required int32 sequence_number = 3;</code>
         */
        boolean hasSequenceNumber();
        /**
         * <code>required int32 sequence_number = 3;</code>
         */
        int getSequenceNumber();

        /**
         * <code>required bool is_last = 4;</code>
         */
        boolean hasIsLast();
        /**
         * <code>required bool is_last = 4;</code>
         */
        boolean getIsLast();

        /**
         * <code>required double ts_start = 5;</code>
         */
        boolean hasTsStart();
        /**
         * <code>required double ts_start = 5;</code>
         */
        double getTsStart();

        /**
         * <code>required double ts_stop = 6;</code>
         */
        boolean hasTsStop();
        /**
         * <code>required double ts_stop = 6;</code>
         */
        double getTsStop();

        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        java.util.List<SensorsProtobuffer.SensorData>
        getDatasList();
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        SensorsProtobuffer.SensorData getDatas(int index);
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        int getDatasCount();
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        java.util.List<? extends SensorsProtobuffer.SensorDataOrBuilder>
        getDatasOrBuilderList();
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        SensorsProtobuffer.SensorDataOrBuilder getDatasOrBuilder(
                int index);

        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        java.util.List<SensorsProtobuffer.SensorInfo>
        getInfoList();
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        SensorsProtobuffer.SensorInfo getInfo(int index);
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        int getInfoCount();
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        java.util.List<? extends SensorsProtobuffer.SensorInfoOrBuilder>
        getInfoOrBuilderList();
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        SensorsProtobuffer.SensorInfoOrBuilder getInfoOrBuilder(
                int index);

        /**
         * <code>optional string meta = 9;</code>
         */
        boolean hasMeta();
        /**
         * <code>optional string meta = 9;</code>
         */
        java.lang.String getMeta();
        /**
         * <code>optional string meta = 9;</code>
         */
        com.google.protobuf.ByteString
        getMetaBytes();

        /**
         * <code>optional string timezone = 10;</code>
         */
        boolean hasTimezone();
        /**
         * <code>optional string timezone = 10;</code>
         */
        java.lang.String getTimezone();
        /**
         * <code>optional string timezone = 10;</code>
         */
        com.google.protobuf.ByteString
        getTimezoneBytes();

        /**
         * <code>optional int32 delay = 11;</code>
         */
        boolean hasDelay();
        /**
         * <code>optional int32 delay = 11;</code>
         */
        int getDelay();
    }
    /**
     * Protobuf type {@code TrackSplit}
     */
    public static final class TrackSplit extends
            com.google.protobuf.GeneratedMessage implements
            // @@protoc_insertion_point(message_implements:TrackSplit)
            TrackSplitOrBuilder {
        // Use TrackSplit.newBuilder() to construct.
        private TrackSplit(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }
        private TrackSplit(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

        private static final TrackSplit defaultInstance;
        public static TrackSplit getDefaultInstance() {
            return defaultInstance;
        }

        public TrackSplit getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;
        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }
        private TrackSplit(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        default: {
                            if (!parseUnknownField(input, unknownFields,
                                    extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                        case 10: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000001;
                            phoneId_ = bs;
                            break;
                        }
                        case 18: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000002;
                            trackUid_ = bs;
                            break;
                        }
                        case 24: {
                            bitField0_ |= 0x00000004;
                            sequenceNumber_ = input.readInt32();
                            break;
                        }
                        case 32: {
                            bitField0_ |= 0x00000008;
                            isLast_ = input.readBool();
                            break;
                        }
                        case 41: {
                            bitField0_ |= 0x00000010;
                            tsStart_ = input.readDouble();
                            break;
                        }
                        case 49: {
                            bitField0_ |= 0x00000020;
                            tsStop_ = input.readDouble();
                            break;
                        }
                        case 58: {
                            if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                                datas_ = new java.util.ArrayList<SensorsProtobuffer.SensorData>();
                                mutable_bitField0_ |= 0x00000040;
                            }
                            datas_.add(input.readMessage(SensorsProtobuffer.SensorData.PARSER, extensionRegistry));
                            break;
                        }
                        case 66: {
                            if (!((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
                                info_ = new java.util.ArrayList<SensorsProtobuffer.SensorInfo>();
                                mutable_bitField0_ |= 0x00000080;
                            }
                            info_.add(input.readMessage(SensorsProtobuffer.SensorInfo.PARSER, extensionRegistry));
                            break;
                        }
                        case 74: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000040;
                            meta_ = bs;
                            break;
                        }
                        case 82: {
                            com.google.protobuf.ByteString bs = input.readBytes();
                            bitField0_ |= 0x00000080;
                            timezone_ = bs;
                            break;
                        }
                        case 88: {
                            bitField0_ |= 0x00000100;
                            delay_ = input.readInt32();
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                    datas_ = java.util.Collections.unmodifiableList(datas_);
                }
                if (((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
                    info_ = java.util.Collections.unmodifiableList(info_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }
        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return SensorsProtobuffer.internal_static_TrackSplit_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return SensorsProtobuffer.internal_static_TrackSplit_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            SensorsProtobuffer.TrackSplit.class, SensorsProtobuffer.TrackSplit.Builder.class);
        }

        public static com.google.protobuf.Parser<TrackSplit> PARSER =
                new com.google.protobuf.AbstractParser<TrackSplit>() {
                    public TrackSplit parsePartialFrom(
                            com.google.protobuf.CodedInputStream input,
                            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                            throws com.google.protobuf.InvalidProtocolBufferException {
                        return new TrackSplit(input, extensionRegistry);
                    }
                };

        @java.lang.Override
        public com.google.protobuf.Parser<TrackSplit> getParserForType() {
            return PARSER;
        }

        private int bitField0_;
        public static final int PHONE_ID_FIELD_NUMBER = 1;
        private java.lang.Object phoneId_;
        /**
         * <code>required string phone_id = 1;</code>
         */
        public boolean hasPhoneId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>required string phone_id = 1;</code>
         */
        public java.lang.String getPhoneId() {
            java.lang.Object ref = phoneId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    phoneId_ = s;
                }
                return s;
            }
        }
        /**
         * <code>required string phone_id = 1;</code>
         */
        public com.google.protobuf.ByteString
        getPhoneIdBytes() {
            java.lang.Object ref = phoneId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                phoneId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int TRACK_UID_FIELD_NUMBER = 2;
        private java.lang.Object trackUid_;
        /**
         * <code>required string track_uid = 2;</code>
         */
        public boolean hasTrackUid() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>required string track_uid = 2;</code>
         */
        public java.lang.String getTrackUid() {
            java.lang.Object ref = trackUid_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    trackUid_ = s;
                }
                return s;
            }
        }
        /**
         * <code>required string track_uid = 2;</code>
         */
        public com.google.protobuf.ByteString
        getTrackUidBytes() {
            java.lang.Object ref = trackUid_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                trackUid_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int SEQUENCE_NUMBER_FIELD_NUMBER = 3;
        private int sequenceNumber_;
        /**
         * <code>required int32 sequence_number = 3;</code>
         */
        public boolean hasSequenceNumber() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }
        /**
         * <code>required int32 sequence_number = 3;</code>
         */
        public int getSequenceNumber() {
            return sequenceNumber_;
        }

        public static final int IS_LAST_FIELD_NUMBER = 4;
        private boolean isLast_;
        /**
         * <code>required bool is_last = 4;</code>
         */
        public boolean hasIsLast() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }
        /**
         * <code>required bool is_last = 4;</code>
         */
        public boolean getIsLast() {
            return isLast_;
        }

        public static final int TS_START_FIELD_NUMBER = 5;
        private double tsStart_;
        /**
         * <code>required double ts_start = 5;</code>
         */
        public boolean hasTsStart() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }
        /**
         * <code>required double ts_start = 5;</code>
         */
        public double getTsStart() {
            return tsStart_;
        }

        public static final int TS_STOP_FIELD_NUMBER = 6;
        private double tsStop_;
        /**
         * <code>required double ts_stop = 6;</code>
         */
        public boolean hasTsStop() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }
        /**
         * <code>required double ts_stop = 6;</code>
         */
        public double getTsStop() {
            return tsStop_;
        }

        public static final int DATAS_FIELD_NUMBER = 7;
        private java.util.List<SensorsProtobuffer.SensorData> datas_;
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        public java.util.List<SensorsProtobuffer.SensorData> getDatasList() {
            return datas_;
        }
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        public java.util.List<? extends SensorsProtobuffer.SensorDataOrBuilder>
        getDatasOrBuilderList() {
            return datas_;
        }
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        public int getDatasCount() {
            return datas_.size();
        }
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        public SensorsProtobuffer.SensorData getDatas(int index) {
            return datas_.get(index);
        }
        /**
         * <code>repeated .SensorData datas = 7;</code>
         */
        public SensorsProtobuffer.SensorDataOrBuilder getDatasOrBuilder(
                int index) {
            return datas_.get(index);
        }

        public static final int INFO_FIELD_NUMBER = 8;
        private java.util.List<SensorsProtobuffer.SensorInfo> info_;
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        public java.util.List<SensorsProtobuffer.SensorInfo> getInfoList() {
            return info_;
        }
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        public java.util.List<? extends SensorsProtobuffer.SensorInfoOrBuilder>
        getInfoOrBuilderList() {
            return info_;
        }
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        public int getInfoCount() {
            return info_.size();
        }
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        public SensorsProtobuffer.SensorInfo getInfo(int index) {
            return info_.get(index);
        }
        /**
         * <code>repeated .SensorInfo info = 8;</code>
         */
        public SensorsProtobuffer.SensorInfoOrBuilder getInfoOrBuilder(
                int index) {
            return info_.get(index);
        }

        public static final int META_FIELD_NUMBER = 9;
        private java.lang.Object meta_;
        /**
         * <code>optional string meta = 9;</code>
         */
        public boolean hasMeta() {
            return ((bitField0_ & 0x00000040) == 0x00000040);
        }
        /**
         * <code>optional string meta = 9;</code>
         */
        public java.lang.String getMeta() {
            java.lang.Object ref = meta_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    meta_ = s;
                }
                return s;
            }
        }
        /**
         * <code>optional string meta = 9;</code>
         */
        public com.google.protobuf.ByteString
        getMetaBytes() {
            java.lang.Object ref = meta_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                meta_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int TIMEZONE_FIELD_NUMBER = 10;
        private java.lang.Object timezone_;
        /**
         * <code>optional string timezone = 10;</code>
         */
        public boolean hasTimezone() {
            return ((bitField0_ & 0x00000080) == 0x00000080);
        }
        /**
         * <code>optional string timezone = 10;</code>
         */
        public java.lang.String getTimezone() {
            java.lang.Object ref = timezone_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    timezone_ = s;
                }
                return s;
            }
        }
        /**
         * <code>optional string timezone = 10;</code>
         */
        public com.google.protobuf.ByteString
        getTimezoneBytes() {
            java.lang.Object ref = timezone_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                timezone_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int DELAY_FIELD_NUMBER = 11;
        private int delay_;
        /**
         * <code>optional int32 delay = 11;</code>
         */
        public boolean hasDelay() {
            return ((bitField0_ & 0x00000100) == 0x00000100);
        }
        /**
         * <code>optional int32 delay = 11;</code>
         */
        public int getDelay() {
            return delay_;
        }

        private void initFields() {
            phoneId_ = "";
            trackUid_ = "";
            sequenceNumber_ = 0;
            isLast_ = false;
            tsStart_ = 0D;
            tsStop_ = 0D;
            datas_ = java.util.Collections.emptyList();
            info_ = java.util.Collections.emptyList();
            meta_ = "";
            timezone_ = "";
            delay_ = 0;
        }
        private byte memoizedIsInitialized = -1;
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            if (!hasPhoneId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTrackUid()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasSequenceNumber()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasIsLast()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTsStart()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTsStop()) {
                memoizedIsInitialized = 0;
                return false;
            }
            for (int i = 0; i < getDatasCount(); i++) {
                if (!getDatas(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            for (int i = 0; i < getInfoCount(); i++) {
                if (!getInfo(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getPhoneIdBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getTrackUidBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(3, sequenceNumber_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBool(4, isLast_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeDouble(5, tsStart_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeDouble(6, tsStop_);
            }
            for (int i = 0; i < datas_.size(); i++) {
                output.writeMessage(7, datas_.get(i));
            }
            for (int i = 0; i < info_.size(); i++) {
                output.writeMessage(8, info_.get(i));
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                output.writeBytes(9, getMetaBytes());
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                output.writeBytes(10, getTimezoneBytes());
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                output.writeInt32(11, delay_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;
        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1) return size;

            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(1, getPhoneIdBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(2, getTrackUidBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(3, sequenceNumber_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBoolSize(4, isLast_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeDoubleSize(5, tsStart_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeDoubleSize(6, tsStop_);
            }
            for (int i = 0; i < datas_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(7, datas_.get(i));
            }
            for (int i = 0; i < info_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(8, info_.get(i));
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(9, getMetaBytes());
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(10, getTimezoneBytes());
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(11, delay_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;
        @java.lang.Override
        protected java.lang.Object writeReplace()
                throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        public static SensorsProtobuffer.TrackSplit parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.TrackSplit parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }
        public static SensorsProtobuffer.TrackSplit parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return PARSER.parseFrom(input);
        }
        public static SensorsProtobuffer.TrackSplit parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() { return Builder.create(); }
        public Builder newBuilderForType() { return newBuilder(); }
        public static Builder newBuilder(SensorsProtobuffer.TrackSplit prototype) {
            return newBuilder().mergeFrom(prototype);
        }
        public Builder toBuilder() { return newBuilder(this); }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }
        /**
         * Protobuf type {@code TrackSplit}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessage.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:TrackSplit)
                SensorsProtobuffer.TrackSplitOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return SensorsProtobuffer.internal_static_TrackSplit_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return SensorsProtobuffer.internal_static_TrackSplit_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                SensorsProtobuffer.TrackSplit.class, SensorsProtobuffer.TrackSplit.Builder.class);
            }

            // Construct using Track.TrackSplit.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }
            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getDatasFieldBuilder();
                    getInfoFieldBuilder();
                }
            }
            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                phoneId_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                trackUid_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                sequenceNumber_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                isLast_ = false;
                bitField0_ = (bitField0_ & ~0x00000008);
                tsStart_ = 0D;
                bitField0_ = (bitField0_ & ~0x00000010);
                tsStop_ = 0D;
                bitField0_ = (bitField0_ & ~0x00000020);
                if (datasBuilder_ == null) {
                    datas_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000040);
                } else {
                    datasBuilder_.clear();
                }
                if (infoBuilder_ == null) {
                    info_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000080);
                } else {
                    infoBuilder_.clear();
                }
                meta_ = "";
                bitField0_ = (bitField0_ & ~0x00000100);
                timezone_ = "";
                bitField0_ = (bitField0_ & ~0x00000200);
                delay_ = 0;
                bitField0_ = (bitField0_ & ~0x00000400);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return SensorsProtobuffer.internal_static_TrackSplit_descriptor;
            }

            public SensorsProtobuffer.TrackSplit getDefaultInstanceForType() {
                return SensorsProtobuffer.TrackSplit.getDefaultInstance();
            }

            public SensorsProtobuffer.TrackSplit build() {
                SensorsProtobuffer.TrackSplit result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public SensorsProtobuffer.TrackSplit buildPartial() {
                SensorsProtobuffer.TrackSplit result = new SensorsProtobuffer.TrackSplit(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.phoneId_ = phoneId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.trackUid_ = trackUid_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.sequenceNumber_ = sequenceNumber_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.isLast_ = isLast_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.tsStart_ = tsStart_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.tsStop_ = tsStop_;
                if (datasBuilder_ == null) {
                    if (((bitField0_ & 0x00000040) == 0x00000040)) {
                        datas_ = java.util.Collections.unmodifiableList(datas_);
                        bitField0_ = (bitField0_ & ~0x00000040);
                    }
                    result.datas_ = datas_;
                } else {
                    result.datas_ = datasBuilder_.build();
                }
                if (infoBuilder_ == null) {
                    if (((bitField0_ & 0x00000080) == 0x00000080)) {
                        info_ = java.util.Collections.unmodifiableList(info_);
                        bitField0_ = (bitField0_ & ~0x00000080);
                    }
                    result.info_ = info_;
                } else {
                    result.info_ = infoBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
                    to_bitField0_ |= 0x00000040;
                }
                result.meta_ = meta_;
                if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
                    to_bitField0_ |= 0x00000080;
                }
                result.timezone_ = timezone_;
                if (((from_bitField0_ & 0x00000400) == 0x00000400)) {
                    to_bitField0_ |= 0x00000100;
                }
                result.delay_ = delay_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof SensorsProtobuffer.TrackSplit) {
                    return mergeFrom((SensorsProtobuffer.TrackSplit)other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(SensorsProtobuffer.TrackSplit other) {
                if (other == SensorsProtobuffer.TrackSplit.getDefaultInstance()) return this;
                if (other.hasPhoneId()) {
                    bitField0_ |= 0x00000001;
                    phoneId_ = other.phoneId_;
                    onChanged();
                }
                if (other.hasTrackUid()) {
                    bitField0_ |= 0x00000002;
                    trackUid_ = other.trackUid_;
                    onChanged();
                }
                if (other.hasSequenceNumber()) {
                    setSequenceNumber(other.getSequenceNumber());
                }
                if (other.hasIsLast()) {
                    setIsLast(other.getIsLast());
                }
                if (other.hasTsStart()) {
                    setTsStart(other.getTsStart());
                }
                if (other.hasTsStop()) {
                    setTsStop(other.getTsStop());
                }
                if (datasBuilder_ == null) {
                    if (!other.datas_.isEmpty()) {
                        if (datas_.isEmpty()) {
                            datas_ = other.datas_;
                            bitField0_ = (bitField0_ & ~0x00000040);
                        } else {
                            ensureDatasIsMutable();
                            datas_.addAll(other.datas_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.datas_.isEmpty()) {
                        if (datasBuilder_.isEmpty()) {
                            datasBuilder_.dispose();
                            datasBuilder_ = null;
                            datas_ = other.datas_;
                            bitField0_ = (bitField0_ & ~0x00000040);
                            datasBuilder_ =
                                    com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                                            getDatasFieldBuilder() : null;
                        } else {
                            datasBuilder_.addAllMessages(other.datas_);
                        }
                    }
                }
                if (infoBuilder_ == null) {
                    if (!other.info_.isEmpty()) {
                        if (info_.isEmpty()) {
                            info_ = other.info_;
                            bitField0_ = (bitField0_ & ~0x00000080);
                        } else {
                            ensureInfoIsMutable();
                            info_.addAll(other.info_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.info_.isEmpty()) {
                        if (infoBuilder_.isEmpty()) {
                            infoBuilder_.dispose();
                            infoBuilder_ = null;
                            info_ = other.info_;
                            bitField0_ = (bitField0_ & ~0x00000080);
                            infoBuilder_ =
                                    com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ?
                                            getInfoFieldBuilder() : null;
                        } else {
                            infoBuilder_.addAllMessages(other.info_);
                        }
                    }
                }
                if (other.hasMeta()) {
                    bitField0_ |= 0x00000100;
                    meta_ = other.meta_;
                    onChanged();
                }
                if (other.hasTimezone()) {
                    bitField0_ |= 0x00000200;
                    timezone_ = other.timezone_;
                    onChanged();
                }
                if (other.hasDelay()) {
                    setDelay(other.getDelay());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasPhoneId()) {

                    return false;
                }
                if (!hasTrackUid()) {

                    return false;
                }
                if (!hasSequenceNumber()) {

                    return false;
                }
                if (!hasIsLast()) {

                    return false;
                }
                if (!hasTsStart()) {

                    return false;
                }
                if (!hasTsStop()) {

                    return false;
                }
                for (int i = 0; i < getDatasCount(); i++) {
                    if (!getDatas(i).isInitialized()) {

                        return false;
                    }
                }
                for (int i = 0; i < getInfoCount(); i++) {
                    if (!getInfo(i).isInitialized()) {

                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                SensorsProtobuffer.TrackSplit parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (SensorsProtobuffer.TrackSplit) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
            private int bitField0_;

            private java.lang.Object phoneId_ = "";
            /**
             * <code>required string phone_id = 1;</code>
             */
            public boolean hasPhoneId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }
            /**
             * <code>required string phone_id = 1;</code>
             */
            public java.lang.String getPhoneId() {
                java.lang.Object ref = phoneId_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        phoneId_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>required string phone_id = 1;</code>
             */
            public com.google.protobuf.ByteString
            getPhoneIdBytes() {
                java.lang.Object ref = phoneId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    phoneId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>required string phone_id = 1;</code>
             */
            public Builder setPhoneId(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                phoneId_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required string phone_id = 1;</code>
             */
            public Builder clearPhoneId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                phoneId_ = getDefaultInstance().getPhoneId();
                onChanged();
                return this;
            }
            /**
             * <code>required string phone_id = 1;</code>
             */
            public Builder setPhoneIdBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                phoneId_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object trackUid_ = "";
            /**
             * <code>required string track_uid = 2;</code>
             */
            public boolean hasTrackUid() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }
            /**
             * <code>required string track_uid = 2;</code>
             */
            public java.lang.String getTrackUid() {
                java.lang.Object ref = trackUid_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        trackUid_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>required string track_uid = 2;</code>
             */
            public com.google.protobuf.ByteString
            getTrackUidBytes() {
                java.lang.Object ref = trackUid_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    trackUid_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>required string track_uid = 2;</code>
             */
            public Builder setTrackUid(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                trackUid_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required string track_uid = 2;</code>
             */
            public Builder clearTrackUid() {
                bitField0_ = (bitField0_ & ~0x00000002);
                trackUid_ = getDefaultInstance().getTrackUid();
                onChanged();
                return this;
            }
            /**
             * <code>required string track_uid = 2;</code>
             */
            public Builder setTrackUidBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                trackUid_ = value;
                onChanged();
                return this;
            }

            private int sequenceNumber_ ;
            /**
             * <code>required int32 sequence_number = 3;</code>
             */
            public boolean hasSequenceNumber() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }
            /**
             * <code>required int32 sequence_number = 3;</code>
             */
            public int getSequenceNumber() {
                return sequenceNumber_;
            }
            /**
             * <code>required int32 sequence_number = 3;</code>
             */
            public Builder setSequenceNumber(int value) {
                bitField0_ |= 0x00000004;
                sequenceNumber_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required int32 sequence_number = 3;</code>
             */
            public Builder clearSequenceNumber() {
                bitField0_ = (bitField0_ & ~0x00000004);
                sequenceNumber_ = 0;
                onChanged();
                return this;
            }

            private boolean isLast_ ;
            /**
             * <code>required bool is_last = 4;</code>
             */
            public boolean hasIsLast() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }
            /**
             * <code>required bool is_last = 4;</code>
             */
            public boolean getIsLast() {
                return isLast_;
            }
            /**
             * <code>required bool is_last = 4;</code>
             */
            public Builder setIsLast(boolean value) {
                bitField0_ |= 0x00000008;
                isLast_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required bool is_last = 4;</code>
             */
            public Builder clearIsLast() {
                bitField0_ = (bitField0_ & ~0x00000008);
                isLast_ = false;
                onChanged();
                return this;
            }

            private double tsStart_ ;
            /**
             * <code>required double ts_start = 5;</code>
             */
            public boolean hasTsStart() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }
            /**
             * <code>required double ts_start = 5;</code>
             */
            public double getTsStart() {
                return tsStart_;
            }
            /**
             * <code>required double ts_start = 5;</code>
             */
            public Builder setTsStart(double value) {
                bitField0_ |= 0x00000010;
                tsStart_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required double ts_start = 5;</code>
             */
            public Builder clearTsStart() {
                bitField0_ = (bitField0_ & ~0x00000010);
                tsStart_ = 0D;
                onChanged();
                return this;
            }

            private double tsStop_ ;
            /**
             * <code>required double ts_stop = 6;</code>
             */
            public boolean hasTsStop() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }
            /**
             * <code>required double ts_stop = 6;</code>
             */
            public double getTsStop() {
                return tsStop_;
            }
            /**
             * <code>required double ts_stop = 6;</code>
             */
            public Builder setTsStop(double value) {
                bitField0_ |= 0x00000020;
                tsStop_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>required double ts_stop = 6;</code>
             */
            public Builder clearTsStop() {
                bitField0_ = (bitField0_ & ~0x00000020);
                tsStop_ = 0D;
                onChanged();
                return this;
            }

            private java.util.List<SensorsProtobuffer.SensorData> datas_ =
                    java.util.Collections.emptyList();
            private void ensureDatasIsMutable() {
                if (!((bitField0_ & 0x00000040) == 0x00000040)) {
                    datas_ = new java.util.ArrayList<SensorsProtobuffer.SensorData>(datas_);
                    bitField0_ |= 0x00000040;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<
                    SensorsProtobuffer.SensorData, SensorsProtobuffer.SensorData.Builder, SensorsProtobuffer.SensorDataOrBuilder> datasBuilder_;

            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public java.util.List<SensorsProtobuffer.SensorData> getDatasList() {
                if (datasBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(datas_);
                } else {
                    return datasBuilder_.getMessageList();
                }
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public int getDatasCount() {
                if (datasBuilder_ == null) {
                    return datas_.size();
                } else {
                    return datasBuilder_.getCount();
                }
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public SensorsProtobuffer.SensorData getDatas(int index) {
                if (datasBuilder_ == null) {
                    return datas_.get(index);
                } else {
                    return datasBuilder_.getMessage(index);
                }
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder setDatas(
                    int index, SensorsProtobuffer.SensorData value) {
                if (datasBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureDatasIsMutable();
                    datas_.set(index, value);
                    onChanged();
                } else {
                    datasBuilder_.setMessage(index, value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder setDatas(
                    int index, SensorsProtobuffer.SensorData.Builder builderForValue) {
                if (datasBuilder_ == null) {
                    ensureDatasIsMutable();
                    datas_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    datasBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder addDatas(SensorsProtobuffer.SensorData value) {
                if (datasBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureDatasIsMutable();
                    datas_.add(value);
                    onChanged();
                } else {
                    datasBuilder_.addMessage(value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder addDatas(
                    int index, SensorsProtobuffer.SensorData value) {
                if (datasBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureDatasIsMutable();
                    datas_.add(index, value);
                    onChanged();
                } else {
                    datasBuilder_.addMessage(index, value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder addDatas(
                    SensorsProtobuffer.SensorData.Builder builderForValue) {
                if (datasBuilder_ == null) {
                    ensureDatasIsMutable();
                    datas_.add(builderForValue.build());
                    onChanged();
                } else {
                    datasBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder addDatas(
                    int index, SensorsProtobuffer.SensorData.Builder builderForValue) {
                if (datasBuilder_ == null) {
                    ensureDatasIsMutable();
                    datas_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    datasBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder addAllDatas(
                    java.lang.Iterable<? extends SensorsProtobuffer.SensorData> values) {
                if (datasBuilder_ == null) {
                    ensureDatasIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, datas_);
                    onChanged();
                } else {
                    datasBuilder_.addAllMessages(values);
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder clearDatas() {
                if (datasBuilder_ == null) {
                    datas_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000040);
                    onChanged();
                } else {
                    datasBuilder_.clear();
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public Builder removeDatas(int index) {
                if (datasBuilder_ == null) {
                    ensureDatasIsMutable();
                    datas_.remove(index);
                    onChanged();
                } else {
                    datasBuilder_.remove(index);
                }
                return this;
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public SensorsProtobuffer.SensorData.Builder getDatasBuilder(
                    int index) {
                return getDatasFieldBuilder().getBuilder(index);
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public SensorsProtobuffer.SensorDataOrBuilder getDatasOrBuilder(
                    int index) {
                if (datasBuilder_ == null) {
                    return datas_.get(index);  } else {
                    return datasBuilder_.getMessageOrBuilder(index);
                }
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public java.util.List<? extends SensorsProtobuffer.SensorDataOrBuilder>
            getDatasOrBuilderList() {
                if (datasBuilder_ != null) {
                    return datasBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(datas_);
                }
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public SensorsProtobuffer.SensorData.Builder addDatasBuilder() {
                return getDatasFieldBuilder().addBuilder(
                        SensorsProtobuffer.SensorData.getDefaultInstance());
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public SensorsProtobuffer.SensorData.Builder addDatasBuilder(
                    int index) {
                return getDatasFieldBuilder().addBuilder(
                        index, SensorsProtobuffer.SensorData.getDefaultInstance());
            }
            /**
             * <code>repeated .SensorData datas = 7;</code>
             */
            public java.util.List<SensorsProtobuffer.SensorData.Builder>
            getDatasBuilderList() {
                return getDatasFieldBuilder().getBuilderList();
            }
            private com.google.protobuf.RepeatedFieldBuilder<
                    SensorsProtobuffer.SensorData, SensorsProtobuffer.SensorData.Builder, SensorsProtobuffer.SensorDataOrBuilder>
            getDatasFieldBuilder() {
                if (datasBuilder_ == null) {
                    datasBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                            SensorsProtobuffer.SensorData, SensorsProtobuffer.SensorData.Builder, SensorsProtobuffer.SensorDataOrBuilder>(
                            datas_,
                            ((bitField0_ & 0x00000040) == 0x00000040),
                            getParentForChildren(),
                            isClean());
                    datas_ = null;
                }
                return datasBuilder_;
            }

            private java.util.List<SensorsProtobuffer.SensorInfo> info_ =
                    java.util.Collections.emptyList();
            private void ensureInfoIsMutable() {
                if (!((bitField0_ & 0x00000080) == 0x00000080)) {
                    info_ = new java.util.ArrayList<SensorsProtobuffer.SensorInfo>(info_);
                    bitField0_ |= 0x00000080;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<
                    SensorsProtobuffer.SensorInfo, SensorsProtobuffer.SensorInfo.Builder, SensorsProtobuffer.SensorInfoOrBuilder> infoBuilder_;

            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public java.util.List<SensorsProtobuffer.SensorInfo> getInfoList() {
                if (infoBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(info_);
                } else {
                    return infoBuilder_.getMessageList();
                }
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public int getInfoCount() {
                if (infoBuilder_ == null) {
                    return info_.size();
                } else {
                    return infoBuilder_.getCount();
                }
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public SensorsProtobuffer.SensorInfo getInfo(int index) {
                if (infoBuilder_ == null) {
                    return info_.get(index);
                } else {
                    return infoBuilder_.getMessage(index);
                }
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder setInfo(
                    int index, SensorsProtobuffer.SensorInfo value) {
                if (infoBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInfoIsMutable();
                    info_.set(index, value);
                    onChanged();
                } else {
                    infoBuilder_.setMessage(index, value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder setInfo(
                    int index, SensorsProtobuffer.SensorInfo.Builder builderForValue) {
                if (infoBuilder_ == null) {
                    ensureInfoIsMutable();
                    info_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    infoBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder addInfo(SensorsProtobuffer.SensorInfo value) {
                if (infoBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInfoIsMutable();
                    info_.add(value);
                    onChanged();
                } else {
                    infoBuilder_.addMessage(value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder addInfo(
                    int index, SensorsProtobuffer.SensorInfo value) {
                if (infoBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInfoIsMutable();
                    info_.add(index, value);
                    onChanged();
                } else {
                    infoBuilder_.addMessage(index, value);
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder addInfo(
                    SensorsProtobuffer.SensorInfo.Builder builderForValue) {
                if (infoBuilder_ == null) {
                    ensureInfoIsMutable();
                    info_.add(builderForValue.build());
                    onChanged();
                } else {
                    infoBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder addInfo(
                    int index, SensorsProtobuffer.SensorInfo.Builder builderForValue) {
                if (infoBuilder_ == null) {
                    ensureInfoIsMutable();
                    info_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    infoBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder addAllInfo(
                    java.lang.Iterable<? extends SensorsProtobuffer.SensorInfo> values) {
                if (infoBuilder_ == null) {
                    ensureInfoIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, info_);
                    onChanged();
                } else {
                    infoBuilder_.addAllMessages(values);
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder clearInfo() {
                if (infoBuilder_ == null) {
                    info_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000080);
                    onChanged();
                } else {
                    infoBuilder_.clear();
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public Builder removeInfo(int index) {
                if (infoBuilder_ == null) {
                    ensureInfoIsMutable();
                    info_.remove(index);
                    onChanged();
                } else {
                    infoBuilder_.remove(index);
                }
                return this;
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public SensorsProtobuffer.SensorInfo.Builder getInfoBuilder(
                    int index) {
                return getInfoFieldBuilder().getBuilder(index);
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public SensorsProtobuffer.SensorInfoOrBuilder getInfoOrBuilder(
                    int index) {
                if (infoBuilder_ == null) {
                    return info_.get(index);  } else {
                    return infoBuilder_.getMessageOrBuilder(index);
                }
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public java.util.List<? extends SensorsProtobuffer.SensorInfoOrBuilder>
            getInfoOrBuilderList() {
                if (infoBuilder_ != null) {
                    return infoBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(info_);
                }
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public SensorsProtobuffer.SensorInfo.Builder addInfoBuilder() {
                return getInfoFieldBuilder().addBuilder(
                        SensorsProtobuffer.SensorInfo.getDefaultInstance());
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public SensorsProtobuffer.SensorInfo.Builder addInfoBuilder(
                    int index) {
                return getInfoFieldBuilder().addBuilder(
                        index, SensorsProtobuffer.SensorInfo.getDefaultInstance());
            }
            /**
             * <code>repeated .SensorInfo info = 8;</code>
             */
            public java.util.List<SensorsProtobuffer.SensorInfo.Builder>
            getInfoBuilderList() {
                return getInfoFieldBuilder().getBuilderList();
            }
            private com.google.protobuf.RepeatedFieldBuilder<
                    SensorsProtobuffer.SensorInfo, SensorsProtobuffer.SensorInfo.Builder, SensorsProtobuffer.SensorInfoOrBuilder>
            getInfoFieldBuilder() {
                if (infoBuilder_ == null) {
                    infoBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<
                            SensorsProtobuffer.SensorInfo, SensorsProtobuffer.SensorInfo.Builder, SensorsProtobuffer.SensorInfoOrBuilder>(
                            info_,
                            ((bitField0_ & 0x00000080) == 0x00000080),
                            getParentForChildren(),
                            isClean());
                    info_ = null;
                }
                return infoBuilder_;
            }

            private java.lang.Object meta_ = "";
            /**
             * <code>optional string meta = 9;</code>
             */
            public boolean hasMeta() {
                return ((bitField0_ & 0x00000100) == 0x00000100);
            }
            /**
             * <code>optional string meta = 9;</code>
             */
            public java.lang.String getMeta() {
                java.lang.Object ref = meta_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        meta_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>optional string meta = 9;</code>
             */
            public com.google.protobuf.ByteString
            getMetaBytes() {
                java.lang.Object ref = meta_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    meta_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>optional string meta = 9;</code>
             */
            public Builder setMeta(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000100;
                meta_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional string meta = 9;</code>
             */
            public Builder clearMeta() {
                bitField0_ = (bitField0_ & ~0x00000100);
                meta_ = getDefaultInstance().getMeta();
                onChanged();
                return this;
            }
            /**
             * <code>optional string meta = 9;</code>
             */
            public Builder setMetaBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000100;
                meta_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object timezone_ = "";
            /**
             * <code>optional string timezone = 10;</code>
             */
            public boolean hasTimezone() {
                return ((bitField0_ & 0x00000200) == 0x00000200);
            }
            /**
             * <code>optional string timezone = 10;</code>
             */
            public java.lang.String getTimezone() {
                java.lang.Object ref = timezone_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        timezone_ = s;
                    }
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }
            /**
             * <code>optional string timezone = 10;</code>
             */
            public com.google.protobuf.ByteString
            getTimezoneBytes() {
                java.lang.Object ref = timezone_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    timezone_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }
            /**
             * <code>optional string timezone = 10;</code>
             */
            public Builder setTimezone(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000200;
                timezone_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional string timezone = 10;</code>
             */
            public Builder clearTimezone() {
                bitField0_ = (bitField0_ & ~0x00000200);
                timezone_ = getDefaultInstance().getTimezone();
                onChanged();
                return this;
            }
            /**
             * <code>optional string timezone = 10;</code>
             */
            public Builder setTimezoneBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000200;
                timezone_ = value;
                onChanged();
                return this;
            }

            private int delay_ ;
            /**
             * <code>optional int32 delay = 11;</code>
             */
            public boolean hasDelay() {
                return ((bitField0_ & 0x00000400) == 0x00000400);
            }
            /**
             * <code>optional int32 delay = 11;</code>
             */
            public int getDelay() {
                return delay_;
            }
            /**
             * <code>optional int32 delay = 11;</code>
             */
            public Builder setDelay(int value) {
                bitField0_ |= 0x00000400;
                delay_ = value;
                onChanged();
                return this;
            }
            /**
             * <code>optional int32 delay = 11;</code>
             */
            public Builder clearDelay() {
                bitField0_ = (bitField0_ & ~0x00000400);
                delay_ = 0;
                onChanged();
                return this;
            }

            // @@protoc_insertion_point(builder_scope:TrackSplit)
        }

        static {
            defaultInstance = new TrackSplit(true);
            defaultInstance.initFields();
        }

        // @@protoc_insertion_point(class_scope:TrackSplit)
    }

    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_SensorData_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_SensorData_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_SensorInfo_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_SensorInfo_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_TrackSplit_descriptor;
    private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internal_static_TrackSplit_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }
    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;
    static {
        java.lang.String[] descriptorData = {
                "\n\"skilo_categorizers/src/track.proto\"z\n\n" +
                        "SensorData\022\n\n\002id\030\001 \002(\005\022\024\n\014sensor_id_fk\030\002" +
                        " \002(\005\022\021\n\005value\030\003 \003(\001B\002\020\001\022\014\n\004text\030\004 \003(\t\022\021\n" +
                        "\ttimestamp\030\005 \002(\001\022\026\n\016packet_counter\030\006 \001(\003" +
                        "\"\252\002\n\nSensorInfo\022\021\n\tsensor_id\030\001 \002(\005\022\014\n\004de" +
                        "sc\030\002 \002(\t\022$\n\004type\030\003 \002(\0162\026.SensorInfo.TYPE" +
                        "SENSOR\022\014\n\004meta\030\004 \001(\t\"\306\001\n\nTYPESENSOR\022\t\n\005O" +
                        "THER\020\000\022\007\n\003ACC\020\001\022\010\n\004GYRO\020\002\022\t\n\005MAGNE\020\003\022\007\n\003" +
                        "BVP\020\004\022\007\n\003GPS\020\005\022\007\n\003BPM\020\006\022\007\n\003GSR\020\007\022\013\n\007BATT" +
                        "ERY\020\010\022\010\n\004TEMP\020\t\022\007\n\003LUX\020\n\022\007\n\003IBI\020\013\022\010\n\004WIF",
                "I\020\014\022\007\n\003GSM\020\r\022\n\n\006MARKER\020\016\022\007\n\003EMG\020\017\022\010\n\004QUA" +
                        "T\020\020\022\007\n\003EEG\020\021\022\007\n\003ECG\020\022\"\344\001\n\nTrackSplit\022\020\n\010" +
                        "phone_id\030\001 \002(\t\022\021\n\ttrack_uid\030\002 \002(\t\022\027\n\017seq" +
                        "uence_number\030\003 \002(\005\022\017\n\007is_last\030\004 \002(\010\022\020\n\010t" +
                        "s_start\030\005 \002(\001\022\017\n\007ts_stop\030\006 \002(\001\022\032\n\005datas\030" +
                        "\007 \003(\0132\013.SensorData\022\031\n\004info\030\010 \003(\0132\013.Senso" +
                        "rInfo\022\014\n\004meta\030\t \001(\t\022\020\n\010timezone\030\n \001(\t\022\r\n" +
                        "\005delay\030\013 \001(\005"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[] {
                        }, assigner);
        internal_static_SensorData_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_SensorData_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_SensorData_descriptor,
                new java.lang.String[] { "Id", "SensorIdFk", "Value", "Text", "Timestamp", "PacketCounter", });
        internal_static_SensorInfo_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_SensorInfo_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_SensorInfo_descriptor,
                new java.lang.String[] { "SensorId", "Desc", "Type", "Meta", });
        internal_static_TrackSplit_descriptor =
                getDescriptor().getMessageTypes().get(2);
        internal_static_TrackSplit_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessage.FieldAccessorTable(
                internal_static_TrackSplit_descriptor,
                new java.lang.String[] { "PhoneId", "TrackUid", "SequenceNumber", "IsLast", "TsStart", "TsStop", "Datas", "Info", "Meta", "Timezone", "Delay", });
    }

    // @@protoc_insertion_point(outer_class_scope)
}
