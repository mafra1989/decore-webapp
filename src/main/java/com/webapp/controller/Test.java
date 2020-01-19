package com.webapp.controller;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class Test {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		@SuppressWarnings("unchecked")
				
		
	 EntityManagerFactory factory;

	  
	        factory = Persistence.createEntityManagerFactory("SmartBankingPU");
	        EntityManager manager = factory.createEntityManager();
	        
	        Calendar calendarStart = Calendar.getInstance();
	        calendarStart.add(Calendar.DAY_OF_MONTH, -5);
	        
	        Calendar calendarStop = Calendar.getInstance();//AND p.mes BETWEEN :mesInicio AND :mesFim AND p.ano BETWEEN :anoInicio AND :anoFim
	        
	        String jpql = "SELECT p.dia, p.mes, p.ano, sum(p.valorTotal) FROM Venda p WHERE p.dia BETWEEN :diaInicio AND :diaFim  group by p.dia, p.mes, p.ano order by p.dia asc, p.mes asc, p.ano asc";
			Query q = manager.createQuery(jpql).setParameter("diaInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.DAY_OF_MONTH))))
					.setParameter("diaFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.DAY_OF_MONTH))));
			
			List<Object[]> result = q.getResultList();
			for (Object[] object : result) {
				if((long)object[0] < 10) {
					object[0] = "0" + object[1];
				}
				
				if((long)object[1] < 10) {
					object[1] = "0" + object[1];
				}
				
				System.out.println(object[0] + "/" + object[1] + "/" + object[2] + " - " + object[3]);
			}
			
		
			
	}
}
