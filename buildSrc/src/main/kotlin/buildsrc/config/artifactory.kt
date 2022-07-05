package buildsrc.config

import org.gradle.kotlin.dsl.delegateClosureOf
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention
import org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask


/** Configure [PublisherConfig] */
fun ArtifactoryPluginConvention.publish(configure: PublisherConfig.() -> Unit) {
    publish(delegateClosureOf<PublisherConfig>(configure))
}

/** Configure [DoubleDelegateWrapper] */
fun PublisherConfig.repository(configure: DoubleDelegateWrapper.() -> Unit) {
    repository(delegateClosureOf<DoubleDelegateWrapper>(configure))
}

/** Configure [ArtifactoryTask] */
fun PublisherConfig.defaults(configure: ArtifactoryTask.() -> Unit) {
    defaults(delegateClosureOf<ArtifactoryTask>(configure))
}
