/*
 * Copyright 2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.android.anko.generator

import org.jetbrains.android.anko.ClassTree
import org.jetbrains.android.anko.MethodNodeWithClass
import org.jetbrains.android.anko.config.AnkoConfiguration
import org.jetbrains.android.anko.config.Configurable
import org.jetbrains.android.anko.fqName
import org.jetbrains.android.anko.packageName
import org.jetbrains.android.anko.utils.ClassTreeUtils
import org.jetbrains.android.anko.utils.ReflectionUtils
import org.objectweb.asm.tree.ClassNode

interface Generator<R> {
    fun generate(state: GenerationState): Iterable<R>
}

class GenerationState(
        public override val classTree: ClassTree,
        config: AnkoConfiguration
): ClassTreeUtils, Configurable(config), ReflectionUtils {

    public val availableClasses: List<ClassNode> = findAvailableClasses()

    public val availableMethods: List<MethodNodeWithClass> = findAvailableMethods(availableClasses)

    private val cachedResults: MutableMap<Class<out Generator<*>>, Iterable<*>> = hashMapOf()

    @suppress("UNCHECKED_CAST")
    fun <T> get(clazz: Class<out Generator<T>>): Iterable<T> = cachedResults.getOrPut(clazz) {
        initializeClass(clazz).generate(this)
    } as Iterable<T>

    public override fun isExcluded(node: ClassNode) =
            node.fqName in config.excludedClasses || "${node.packageName}.*" in config.excludedClasses

    public override fun isExcluded(node: MethodNodeWithClass) =
            (node.clazz.fqName + "#" + node.method.name) in config.excludedMethods

}