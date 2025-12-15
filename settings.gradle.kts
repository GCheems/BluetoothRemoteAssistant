pluginManagement {
    repositories {
        // 使用阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        
        // 备用镜像
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        
        // 官方仓库（作为备用）
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 使用阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        
        // 备用镜像
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        
        // 官方仓库（作为备用）
        google()
        mavenCentral()
    }
}

rootProject.name = "BluetoothRemoteAssistant"
include(":app")
