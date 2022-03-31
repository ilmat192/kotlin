/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.scopes.FirTypeScope
import org.jetbrains.kotlin.fir.scopes.MemberWithBaseScope
import org.jetbrains.kotlin.fir.scopes.impl.filterOutOverriddenFunctions
import org.jetbrains.kotlin.fir.scopes.impl.filterOutOverriddenProperties
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

class ConeCompositeConflictResolver(
    private vararg val conflictResolvers: ConeCallConflictResolver
) : ConeCallConflictResolver() {
    override fun chooseMaximallySpecificCandidates(
        candidates: Set<Candidate>,
        discriminateGenerics: Boolean,
        discriminateAbstracts: Boolean
    ): Set<Candidate> {
        if (candidates.size <= 1) return candidates
        var currentCandidates = filterOverrides(candidates)
        var index = 0
        while (currentCandidates.size > 1 && index < conflictResolvers.size) {
            val conflictResolver = conflictResolvers[index++]
            currentCandidates = conflictResolver.chooseMaximallySpecificCandidates(candidates, discriminateGenerics, discriminateAbstracts)
        }
        return currentCandidates
    }

    private fun filterOverrides(candidates: Set<Candidate>): Set<Candidate> {
        val result = mutableSetOf<Candidate>()
        val candidatesWithScope = mutableMapOf<MemberWithBaseScope<*>, Candidate>()
        for (candidate in candidates) {
            val symbol = candidate.symbol
            val scope = candidate.originScope
            if ((symbol is FirNamedFunctionSymbol || symbol is FirPropertySymbol) && scope is FirTypeScope) {
                candidatesWithScope[MemberWithBaseScope(symbol as FirCallableSymbol<*>, scope)] = candidate
            } else {
                result += candidate
            }
        }

        if (candidatesWithScope.isNotEmpty()) {
            val functionCandidates = candidatesWithScope.keys.filter { it.member is FirNamedFunctionSymbol }
            val propertyCandidates = candidatesWithScope.keys.filter { it.member is FirPropertySymbol }

            @Suppress("UNCHECKED_CAST")
            val filteredFunctions =
                filterOutOverriddenFunctions(functionCandidates as Collection<MemberWithBaseScope<FirNamedFunctionSymbol>>, deep = true)

            @Suppress("UNCHECKED_CAST")
            val filteredProperties =
                filterOutOverriddenProperties(propertyCandidates as Collection<MemberWithBaseScope<FirPropertySymbol>>, deep = true)

            for (memberWithBaseScope in filteredFunctions) {
                result += candidatesWithScope.getValue(memberWithBaseScope)
            }

            for (memberWithBaseScope in filteredProperties) {
                result += candidatesWithScope.getValue(memberWithBaseScope)
            }
        }
        return result
    }
}
