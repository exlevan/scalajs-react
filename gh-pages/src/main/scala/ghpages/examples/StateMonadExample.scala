package ghpages.examples

import ghpages.GhPagesMacros
import ghpages.examples.util.SingleSide
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._

object StateMonadExample {

  def content = SingleSide.Content(source, main())

  lazy val main = addIntro(TodoApp, _(
    "This is the same as the ",
    TodoExample.title,
    " example using state monads for change and effects."))

  val source = GhPagesMacros.exampleSource

  // EXAMPLE:START

  val TodoList = ReactComponentB[List[String]]("TodoList")
    .render_P(items =>
      <.ul(items map (<.li(_))))
    .build

  case class State(items: List[String], text: String)

  val ST = ReactS.Fix[State]                              // Let's use a helper so that we don't have to specify the
                                                          //   state type everywhere.

  def acceptChange(e: ReactEventI) =
    ST.mod(_.copy(text = e.target.value))                 // A pure state modification. State value is provided when run.

  def handleSubmit(e: ReactEventI) = (
    ST.retM(e.preventDefaultCB)                           // Lift a Callback effect into a shape that allows composition
                                                          //   with state modification.
    >>                                                    // Use >> to compose. It's flatMap (>>=) that ignores input.
    ST.mod(s => State(s.items :+ s.text, "")).liftCB      // Here we lift a pure state modification into a shape that
  )                                                       //   allows composition with Callback effects.

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .renderS(($, s) =>
      <.div(
        <.h3("TODO"),
        TodoList(s.items),
        <.form(^.onSubmit ==> $._runState(handleSubmit),  // runState runs a state monad and applies the result.
          <.input(                                        // _runState is similar but takes a function-to-a-state-monad.
            ^.onChange ==> $._runState(acceptChange),     // In these cases, the function will be fed the JS event.
            ^.value := s.text),
          <.button("Add #", s.items.length + 1)))
    ).buildU

  // EXAMPLE:END
}
