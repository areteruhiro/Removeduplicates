plugins {
    id("com.android.application")
    id("com.google.gms.google-services")


}
android {
    namespace = "com.delete.schedule"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.delete.schedule"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES");
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation ("com.google.api-client:google-api-client:1.32.1")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.32.1")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev305-1.32.1")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev201-1.22.0" )
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.13.0") // Jacksonの最新バージョンに置き換える
    implementation ("com.google.code.gson:gson:2.10")
    implementation ("com.google.api-client:google-api-client:1.32.1")
    implementation ("com.google.apis:google-api-services-calendar:v3-rev305-1.25.0")
    implementation ("pub.devrel:easypermissions:3.0.0")
    implementation ("pub.devrel:easypermissions:3.0.0")
    implementation("com.google.api-client:google-api-client-android:1.32.1")

    implementation ("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev305-1.31.5")

    implementation ("com.google.api-client:google-api-client:1.31.5")
    implementation ("com.google.http-client:google-http-client-gson:1.39.1")




}