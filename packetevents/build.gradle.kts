dependencies {
    api(project(":common"))

    compileOnly("com.github.retrooper:packetevents-api:2.9.5")
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
