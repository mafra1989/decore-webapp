PrimeFaces.locales['pt'] = {
	closeText : 'Fechar',
	prevText : 'Anterior',
	nextText : 'Próximo',
	currentText : 'Começo',
	monthNames : [ 'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
			'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro' ],
	monthNamesShort : [ 'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago',
			'Set', 'Out', 'Nov', 'Dez' ],
	dayNames : [ 'Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta',
			'Sábado' ],
	dayNamesShort : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb' ],
	dayNamesMin : [ 'D', 'S', 'T', 'Q', 'Q', 'S', 'S' ],
	weekHeader : 'Semana',
	firstDay : 0,
	isRTL : false,
	showMonthAfterYear : false,
	yearSuffix : '',
	timeOnlyTitle : 'Só Horas',
	timeText : 'Tempo',
	hourText : 'Hora',
	minuteText : 'Minuto',
	secondText : 'Segundo',
	ampm : false,
	month : 'Mês',
	week : 'Semana',
	day : 'Dia',
	allDayText : 'Todo o Dia'
};

function deleteItem(itemID) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Tem certeza?',
		  text: "O registro será apagado do seu banco de dados!",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, delete!',
		  cancelButtonText: 'Não, cancele!',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  setarItemID([ {
					name : 'itemID',
					value : itemID
				} ]);
			  		 
		  }
		})
		
}

function estornarConta(itemID) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Atenção!',
		  text: "Deseja realmente desfazer o pagamento?",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, estorne!',
		  cancelButtonText: 'Não, cancele!',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  setarItemID([ {
					name : 'itemID',
					value : itemID
				} ]);
			  		 
		  }
		})
		
}
