/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.MemberWithBaseScope
import org.jetbrains.kotlin.fir.scopes.ProcessOverriddenWithBaseScope
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

fun filterOutOverriddenFunctions(extractedOverridden: Collection<MemberWithBaseScope<FirNamedFunctionSymbol>>, deep: Boolean = false): Collection<MemberWithBaseScope<FirNamedFunctionSymbol>> {
    return filterOutOverridden(extractedOverridden, FirTypeScope::processDirectOverriddenFunctionsWithBaseScope, deep)
}

fun filterOutOverriddenProperties(extractedOverridden: Collection<MemberWithBaseScope<FirPropertySymbol>>, deep: Boolean = false): Collection<MemberWithBaseScope<FirPropertySymbol>> {
    return filterOutOverridden(extractedOverridden, FirTypeScope::processDirectOverriddenPropertiesWithBaseScope, deep)
}

@OptIn(PrivateForInline::class)
fun <D : FirCallableSymbol<*>> filterOutOverridden(
    extractedOverridden: Collection<MemberWithBaseScope<D>>,
    processAllOverridden: ProcessOverriddenWithBaseScope<D>,
    deep: Boolean
): Collection<MemberWithBaseScope<D>> {
    return extractedOverridden.filter { overridden1 ->
        extractedOverridden.none { overridden2 ->
            overridden1 !== overridden2 && overrides(
                overridden2,
                overridden1,
                processAllOverridden,
                deep
            )
        }
    }
}

// Whether f overrides g
@PrivateForInline
fun <D : FirCallableSymbol<*>> overrides(
    f: MemberWithBaseScope<D>,
    g: MemberWithBaseScope<D>,
    processAllOverridden: ProcessOverriddenWithBaseScope<D>,
    deep: Boolean
): Boolean {
    val (fMember, fScope) = f
    val (gMember) = g

    var result = false

    fun processor(overridden: D, scope: FirTypeScope): ProcessorAction {
        result = result || overridden == gMember
        if (result) {
            return ProcessorAction.STOP
        }
        if (deep) {
            return scope.processAllOverridden(overridden, ::processor)
        }

        return ProcessorAction.NEXT
    }

    fScope.processAllOverridden(fMember, ::processor)

    return result
}
