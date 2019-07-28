package com.java.study.thread.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;

import lombok.Getter;
import lombok.Setter;

public class MyDisruptor {

	public static void main(String[] args) {
		
	}

}

class PersonEventHandler  implements EventHandler<PersonEvent>{
    
    public PersonEventHandler(){
//        DataSendHelper.start();
    }
    @Override
    public void onEvent(PersonEvent event, long sequence, boolean endOfBatch)
            throws Exception {
        Person person = event.getPerson();
        System.out.println("name = "+person.getName()+", age = "+ person.getAge()+", gender = "+ person.getGender() +", mobile = "+person.getMobile());    
    }

}

class PersonEvent {
    
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public final static  EventFactory<PersonEvent> EVENT_FACTORY = new EventFactory<PersonEvent>(){
        public PersonEvent newInstance(){
            return new PersonEvent();
        }
    };
}





@Getter
@Setter
class Person {
    private String name;
    private int age;
    private String gender;
    private String mobile;
    
    
    public Person(String name, int age, String gender, String mobile){
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.mobile = mobile;
    }
    
}