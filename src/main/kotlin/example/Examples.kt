package example

import io.reactivex.*
import io.reactivex.observables.ConnectableObservable
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

// Observable e Observer Hello World
fun example_1_1() {
    // O primeiro Observable do stream é normalmente chamado de source
    // Devem sempre emitir eventos de um mesmo tipo
    val source: Observable<String> = Observable.create { emitter ->
        emitter.onNext("Ev1")
        emitter.onNext("Ev2")
        emitter.onNext("Ev3")
        emitter.onComplete()
    }

    // Observer sem tratamento de erro
    source.subscribe { event -> println("Received: $event") }
}

fun example_1_2() {
    val source: Observable<Int> = Observable.create { emitter ->
        emitter.onNext(10 / 2)
        emitter.onNext(1/ 0)
        emitter.onNext(2)
        emitter.onComplete()
    }

    // Observer com tratamento de erro
    // Fluxo é cortado caso ocorra um erro
    source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") }
    )
}

fun example_1_3() {
    val source: Observable<Int> = Observable.create { emitter ->
        try {
            emitter.onNext(1)
            emitter.onNext(1 / 0)
            emitter.onNext(2)
            emitter.onComplete()
        } catch (e: Throwable) {
            // O erro que será emitido é o que for passado pelo onError
            emitter.onError(Throwable("My custom error"))
        }
    }

    source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") }
    )
}

// Observable Factories
fun example_2_1(){
    val source1: Observable<String> = Observable.just("Ev1", "Ev2", "Ev3")

    source1.subscribe { event -> println("Received from source1: $event") }

    val eventList = listOf<String>("Ev1", "Ev2", "Ev3")
    val source2: Observable<String> = Observable.fromIterable(eventList)

    source2.subscribe { event -> println("Received from source2: $event") }
}

// fromCallable Usage
fun example_2_2() {
    val source: Observable<Int> = Observable.just(1 / 0)

    source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed!") }
    )
}

fun example_2_3() {
    // Erros só são propagados se ocorrerem dentro do stream dos observables
    val source: Observable<Int> = Observable.fromCallable { 1 / 0 }

    source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed!") }
    )
}

// Hot and Cold Observables
fun example_3_1() {
    // Multicasting, transforma o Observable em Hot para que todas as emissões para os observers sejam feitas simultaneamente
    val source: ConnectableObservable<String> = Observable.just(
        "Maça",
        "Banana",
        "Uva",
        "Melancia"
    ).publish()

    // ConnectableObservable não começa a emitir eventos assim que recebe o subscribe
    // Observer 1
    source.subscribe(
        { event -> println("Received 1: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed 1!") }
    )
    // Observer 2
    source.subscribe(
        { event -> println("Received 2: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed 2!") }
    )

    // Inicia a emissão de eventos
    source.connect()
}

// Single, Maybe e Completable
// Single
fun example_4_1() {
    val source: Observable<String> = Observable.just("SingleItem")
    // Transforma um observable em um Single, tendo como parametro o valor para o caso do observable ser empty
    val singleSource: Single<String> = source.first("Empty!")

    // Um observer de Single precisa somente de 2 handles, onSuccess e onError, onCompleted se torna desnecessário por
    // o Single emitir somente 1 evento
    singleSource.subscribe(
        { event -> println("Received 1: $event") },
        { error -> println("Error occurred: ${error.message}") }
    )

    val emptySource = Observable.empty<String>()
    val singleFromEmptySource = emptySource.first("Empty!")
    singleFromEmptySource.subscribe(
        { event -> println("Received 2: $event") },
        { error -> println("Error occurred: ${error.message}") }
    )
}

// Maybe
fun example_4_2() {
    val source: Observable<Int> = Observable.just(1)
    // Transforma um observable em um Maybe
    val maybeSource: Maybe<Int> = source.firstElement()

    maybeSource.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed!") }
    )

    val emptySource = Observable.empty<String>()
    val maybeFromEmptySource = emptySource.firstElement()

    maybeFromEmptySource.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed!") }
    )
}

// Completable
fun example_4_3() {
    val completable = Completable.fromCallable { println("Action executed!") }

    completable.subscribe { println("Completed!") }
}

// Disposables
fun example_5_1() {
    val source: Observable<Int> = Observable.just(1, 2)

    // Um subscribe retorna um Disposable
    val disposable = source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") },
        { println("Completed!") }
    )

    // Para sources finitos o dispose é chamado automaticamente
    println("Is disposed?: ${disposable.isDisposed}")
}

fun example_5_2() {
    // Numeros sequenciais a cada 1 segundo
    val source = Observable.interval(1, TimeUnit.SECONDS)

    val disposable = source.subscribe(
        { event -> println("Received: $event") },
        { error -> println("Error occurred: ${error.message}") }
    )

    // bloqueia thread por 5 seg
    sleep(5000)
    // para as emissões de eventos e libera os recursos
    disposable.dispose()
    // Sem emissões nesse periodo de 3 segundos
    sleep(3000)
}

// Operators
// defaultIfEmpty e switchIfEmpty
fun example_6_1() {
    // Forma mais "reativa" de código
    Observable.just("Azul", "Branco", "Vermelho")
        .filter { it.startsWith("Z") }
        // Emite o valor especificado no caso do Observable ser empty
        .defaultIfEmpty("None!")
        .map { "Value is $it" }
        .subscribe(
            { event -> println("Received: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )

    Observable.fromIterable(
        listOf(1, 2, 3, 4)
    )
        .filter { it > 5 }
        // Caso o observable seja empty troca para o observable passado para o metodo
        .switchIfEmpty( Observable.just(5, 6) )
        .map { "Value is $it" }
        .subscribe(
            { event -> println("Received: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}

// retry
fun example_6_2() {
    Observable.just(5, 2, 4, 0, 3, 2, 8)
        .map { 10 / it }
        .retry(2)
        .subscribe(
            { event -> println("Received: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}

// Combining Operators
// Merge factory and merge operator
fun example_7_1() {
    val src1 = Observable.just("Item", "Other Item")
    val src2 = Observable.just("Another Item")

    // Não garante ordem, embora seja muito provavel em casos de observable cold
    Observable.merge(src1, src2)
        .subscribe(
            { event -> println("Received from factory: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )

    src1.mergeWith(src2)
        .subscribe(
            { event -> println("Received from operator: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}

// flatMap operator
fun example_7_2() {
    Observable.just("Im a string")
        .flatMap { Observable.fromIterable(it.split(" ")) }
        .subscribe(
            { event -> println("Received from operator: $event") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}

// Debugging tools
fun example_8_1() {
    Observable.just("Im a string")
        .flatMap { Observable.fromIterable(it.split(" ")) }
//        .doOnNext { println(it) }
        .map { listOf(it, it.length) }
//        .doOnNext { println(it) }
        .subscribe(
            { event -> println("Words count: ${event[0]} -> ${event[1]}") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}

fun example_8_2() {
    Observable.just("Im a string")
        .flatMap { Observable.fromIterable(it.split(" ")) }
//        .doOnError { println(it.message) }
        .map { listOf(it, it.length / 0) }
//        .doOnError { println(it.message) }
        .subscribe(
            { event -> println("Words count: ${event[0]} -> ${event[1]}") },
            { error -> println("Error occurred: ${error.message}") },
            { println("Completed!") }
        )
}
