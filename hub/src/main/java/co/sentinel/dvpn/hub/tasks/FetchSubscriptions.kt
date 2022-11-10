package co.sentinel.dvpn.hub.tasks

import co.sentinel.cosmos.base.BaseChain
import co.sentinel.cosmos.dao.Account
import co.sentinel.cosmos.network.ChannelBuilder
import co.sentinel.dvpn.domain.core.functional.Either
import co.sentinel.dvpn.hub.exception.formatHubTaskError
import kotlinx.coroutines.guava.await
import sentinel.subscription.v1.Querier
import sentinel.subscription.v1.QueryServiceGrpc
import sentinel.types.v1.StatusOuterClass
import timber.log.Timber
import java.util.concurrent.TimeUnit

object FetchSubscriptions {
    suspend fun execute(account: Account) = kotlin.runCatching {
        val chain = BaseChain.getChain(account.baseChain)
        val stub = QueryServiceGrpc.newFutureStub(ChannelBuilder.getChain(chain))
            .withDeadlineAfter(ChannelBuilder.TIME_OUT.toLong(), TimeUnit.SECONDS)
        val response = stub.querySubscriptionsForAddress(
            Querier.QuerySubscriptionsForAddressRequest.newBuilder()
                .setAddress(account.address)
                .setStatus(StatusOuterClass.Status.STATUS_ACTIVE)
                .build()
        ).await()
        Either.Right(response.subscriptionsList)
    }.onFailure { Timber.e(it) }
        .getOrElse {
            Either.Left(formatHubTaskError(it))
        }

}