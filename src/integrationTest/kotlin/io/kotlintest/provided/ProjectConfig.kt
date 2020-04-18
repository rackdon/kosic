package io.kotlintest.provided

import io.kotlintest.AbstractProjectConfig
import io.kotlintest.extensions.ProjectLevelExtension
import io.kotlintest.spring.SpringAutowireConstructorExtension

/**
 * From the documentation, this class and package is to have constructor injection during test
 * To do this, create an object that is derived from AbstractProjectConfig, name this object ProjectConfig
 * and place it in a package called io.kotlintest.provided. KotlinTest will detect it's presence and use any
 * configuration defined there when executing tests.
 * **/
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions(): List<ProjectLevelExtension> = listOf(SpringAutowireConstructorExtension)
}
