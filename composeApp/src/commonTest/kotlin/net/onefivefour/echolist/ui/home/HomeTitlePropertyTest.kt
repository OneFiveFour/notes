package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: string-resource-extraction, Property 1: Home title parameterization
 *
 * Property 1: Home title parameterization
 *
 * *For any* path string and any non-empty home title string, `titleFromPath(path, homeTitle)`
 * should return `homeTitle` when the path is `"/"` or empty, and `buildBreadcrumbs(path, homeTitle)`
 * should always have `homeTitle` as the label of the first breadcrumb item.
 *
 * **Validates: Requirements 6.2**
 */
class HomeTitlePropertyTest : FunSpec({

    test("Property 1: Home title parameterization") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),  // Random path strings
            Arb.string(1..50)    // Random non-empty home title strings
        ) { path, homeTitle ->
            // Test titleFromPath behavior
            if (path == "/" || path.isEmpty()) {
                titleFromPath(path, homeTitle) shouldBe homeTitle
            }

            // Test buildBreadcrumbs behavior
            val breadcrumbs = buildBreadcrumbs(path, homeTitle)
            
            // The first breadcrumb should always have homeTitle as the label
            breadcrumbs.first().label shouldBe homeTitle
            breadcrumbs.first().path shouldBe "/"
        }
    }
})
