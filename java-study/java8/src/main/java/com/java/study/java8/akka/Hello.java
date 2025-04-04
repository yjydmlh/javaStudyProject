//package com.java.study.java8.akka;
//
//import akka.actor.AbstractActor;
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//
//public class Hello {
//	public static void main(String[] args) {
//        ActorSystem system = ActorSystem.create("actor-demo-java");
//        ActorRef hello = system.actorOf(Props.create(Hello1.class));
//        hello.tell("Bob", ActorRef.noSender());
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) { /* ignore */ }
//        system.stop(hello);
//    }
//
//}
//class Hello1 extends AbstractActor {
//
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof String) {
//            System.out.println("Hello " + message);
//        }
//    }
//
//	@Override
//	public Receive createReceive() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
