plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tangzhen.campusmarket"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.tangzhen.campusmarket"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.swiperefreshlayout)
    // 网络层：Retrofit 负责请求云端商品数据（强制技术点「网络」）
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    // 图片处理：Glide 负责网络图片的下载、缓存与圆角展示（强制技术点「图片处理」）
    implementation(libs.glide)
}
