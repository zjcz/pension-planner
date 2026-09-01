package com.pensionplanner.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

/**
 * GraalVM native-image hints required for libraries that load classes or native
 * code reflectively at runtime:
 *
 * <ul>
 *   <li>JJWT ({@code io.jsonwebtoken}): {@code Jwts}/{@code Keys} load their
 *       implementation classes by name via {@code Classes.forName/newInstance}
 *       (e.g. {@code KeysBridge}, {@code DefaultJwtBuilder}). The GraalVM
 *       reachability-metadata for jjwt only covers its Jackson/Hmac internals.</li>
 *   <li>sqlite-jdbc: the JDBC driver is registered through META-INF/services and its
 *       JNI library ({@code libsqlitejdbc.so}, bundled in the driver jar) is extracted
 *       to a temp file and loaded at runtime, so it must be embedded as a resource.</li>
 * </ul>
 */
public class NativeRuntimeHints {

    private static final List<String> JJWT_IMPL_CLASSES = List.of(
            // Factory classes loaded by name in Jwts/Keys
            "io.jsonwebtoken.impl.DefaultClaimsBuilder",
            "io.jsonwebtoken.impl.DefaultJwtBuilder",
            "io.jsonwebtoken.impl.DefaultJwtHeaderBuilder",
            "io.jsonwebtoken.impl.DefaultJwtParserBuilder",
            "io.jsonwebtoken.impl.security.KeysBridge",
            // Algorithm/curve registries loaded by name in Jwts.SIG/ENC/KEY
            "io.jsonwebtoken.impl.io.StandardCompressionAlgorithms",
            "io.jsonwebtoken.impl.security.StandardCurves",
            "io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms",
            "io.jsonwebtoken.impl.security.StandardHashAlgorithms",
            "io.jsonwebtoken.impl.security.StandardKeyAlgorithms",
            "io.jsonwebtoken.impl.security.StandardKeyOperations",
            "io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms",
            // JWK facility (unused today, registered to be future-proof)
            "io.jsonwebtoken.impl.security.DefaultDynamicJwkBuilder",
            "io.jsonwebtoken.impl.security.DefaultJwkParserBuilder",
            "io.jsonwebtoken.impl.security.DefaultJwkSetBuilder",
            "io.jsonwebtoken.impl.security.DefaultJwkSetParserBuilder",
            "io.jsonwebtoken.impl.security.DefaultKeyOperationBuilder",
            "io.jsonwebtoken.impl.security.DefaultKeyOperationPolicyBuilder",
            "io.jsonwebtoken.impl.security.JwksBridge");

    public static class Registrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerJwtHints(hints);
            registerSqliteHints(hints);
        }

        private void registerJwtHints(RuntimeHints hints) {
            for (String className : JJWT_IMPL_CLASSES) {
                hints.reflection().registerType(
                        TypeReference.of(className),
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            }
        }

        private void registerSqliteHints(RuntimeHints hints) {
            hints.reflection().registerType(
                    TypeReference.of("org.sqlite.JDBC"),
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            // Hibernate dialect loaded by Class.forName from application.yml
            hints.reflection().registerType(
                    TypeReference.of("org.hibernate.community.dialect.SQLiteDialect"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            // Driver registration via java.sql.DriverManager ServiceLoader
            hints.resources().registerPattern("META-INF/services/java.sql.Driver");
            // JNI native library embedded in the driver jar; extracted to a temp
            // file by org.sqlite at runtime (shared image for all Linux archs).
            hints.resources().registerPattern("org/sqlite/native/Linux/.*libsqlitejdbc\\.so");
        }
    }
}