package com.udf.showcase.main.presenter

import com.factorymarket.rxelm.cmd.CancelByClassCmd
import com.factorymarket.rxelm.cmd.Cmd
import com.factorymarket.rxelm.cmd.None
import com.factorymarket.rxelm.contract.RenderableComponent
import com.factorymarket.rxelm.msg.Idle
import com.factorymarket.rxelm.msg.Init
import com.factorymarket.rxelm.msg.Msg
import com.factorymarket.rxelm.program.Program
import com.factorymarket.rxelm.program.ProgramBuilder
import com.udf.showcase.data.IApiService
import com.udf.showcase.main.model.CancelMsg
import com.udf.showcase.main.model.LoadReposCmd
import com.udf.showcase.main.model.MainState
import com.udf.showcase.main.model.RefreshMsg
import com.udf.showcase.main.model.ReposLoadedMsg
import com.udf.showcase.main.view.IMainView
import com.udf.showcase.navigation.Navigator
import io.reactivex.Single
import org.eclipse.egit.github.core.Repository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainPresenter @Inject constructor(
        val view: IMainView,
        programBuilder: ProgramBuilder,
        private val service: IApiService,
        private val navigator: Navigator
) : RenderableComponent<MainState> {

    private val program: Program<MainState> = programBuilder.build(this)

    fun init(initialState: MainState?) {
        program.run(initialState ?: MainState(userName = service.getUserName()))
    }

    override fun update(msg: Msg, state: MainState): Pair<MainState, Cmd> {
        return when (msg) {
            is Init -> state.copy(isLoading = true) to LoadReposCmd(state.userName)
            is ReposLoadedMsg -> state.copy(isLoading = false, reposList = msg.reposList) to None
            is CancelMsg -> state.copy(isLoading = false) to CancelByClassCmd(cmdClass = LoadReposCmd::class)
            is RefreshMsg -> state.copy(isLoading = true, reposList = listOf()) to LoadReposCmd(
                    state.userName
            )
            else -> state to None
        }
    }

    fun render() {
        program.render()
    }

    override fun render(state: MainState) {
        state.apply {
            view.setTitle(state.userName + "'s starred repos")

            if (isLoading) {
                view.showErrorText(false)
                if (reposList.isEmpty()) {
                    view.showProgress()
                }
            } else {
                view.hideProgress()
                if (reposList.isEmpty()) {
                    view.setErrorText("User has no starred repos")
                    view.showErrorText(true)
                }
            }
            view.setRepos(reposList)
        }
    }

    override fun call(cmd: Cmd): Single<Msg> {
        return when (cmd) {
            is LoadReposCmd -> service.getStarredRepos(cmd.userName)
                    .map { repos -> ReposLoadedMsg(repos) }
            else -> Single.just(Idle)
        }
    }

    fun destroy() {
        program.stop()
    }

    fun refresh() {
        program.accept(RefreshMsg)
    }

    fun cancel() {
        program.accept(CancelMsg)
    }

    fun onRepoItemClick(repository: Repository) {
        navigator.goToRepo(repository)
    }

}
