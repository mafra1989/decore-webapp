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

function deleteItemTemporariamente(itemID) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Tem certeza?',
		  text: "O registro será apagado temporariamente!",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, apague!',
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

function desfazerExclusao(itemID) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Tem certeza?',
		  text: "A exclusão será desfeita !",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, desfaça!',
		  cancelButtonText: 'Não, cancele!',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  desfazer([ {
					name : 'itemID',
					value : itemID
				} ]);
			  		 
		  }
		})
		
}

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

function cancelarVenda() {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Tem certeza?',
		  text: "A venda será cancelada e os itens serão removidos!",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, confirmar!',
		  cancelButtonText: 'Não, cancele!',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  cancelaVenda();
			  		 
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

function atualizaPedido() {
	
	let timerInterval;
	Swal.fire({
	  type: 'warning',
	  title: 'Atualizando',
	  html: 'Tente novamente em aguns instantes, estamos atualizando seu pedido.<br/><b></b>',
	  timer: 5000,
	  allowOutsideClick: false,
	  timerProgressBar: true,
	  onBeforeOpen: () => {
	    Swal.showLoading()
	    timerInterval = setInterval(() => {
	      
	    }, 100)
	  },
	  onClose: () => {
	    clearInterval(timerInterval)
	  }
	}).then((result) => {
	  /* Read more about handling dismissals below */
	  if (result.dismiss === Swal.DismissReason.timer) {
		  $(location).attr('href','pagamento.xhtml');
	  }
	})
}

function pedidoEnviado() {
	
	let timerInterval;
	Swal.fire({
	  type: 'success',
	  title: 'Pagamento realizado!',
	  html: 'Obrigado, em breve entraremos em contato, redicionando para a página principal.<br/><b></b>',
	  timer: 10000,
	  allowOutsideClick: false,
	  timerProgressBar: true,
	  onBeforeOpen: () => {
	    Swal.showLoading()
	    timerInterval = setInterval(() => {
	      
	    }, 100)
	  },
	  onClose: () => {
	    clearInterval(timerInterval)
	  }
	}).then((result) => {
	  /* Read more about handling dismissals below */
	  if (result.dismiss === Swal.DismissReason.timer) {
	    console.log('I was closed by the timer');
	    home();
	  }
	})
}

function guessPaymentMethod(event) {
    let cardnumber = document.getElementById("cardNumber").value;

    if (cardnumber.length >= 6) {
        let bin = cardnumber.substring(0,6);
        window.Mercadopago.getPaymentMethod({
            "bin": bin
        }, setPaymentMethod);
    }
};

function setPaymentMethod(status, response) {
    if (status == 200) {
    	console.log(response);
        let paymentMethodId = response[0].id;
        $('.payment_method_id').val(paymentMethodId);
        getInstallments();
    } else {
        alert(`payment method info error: ${response}`);
    }
}

function getInstallments(){
    window.Mercadopago.getInstallments({
        "payment_method_id": $('.payment_method_id').val(),
        "amount": parseFloat($('.transaction_amount').val())

    }, function (status, response) {
        if (status == 200) {
        	console.log(response);
            document.getElementById('installments').options.length = 0;
            response[0].payer_costs.forEach( installment => {
                let opt = document.createElement('option');
                opt.text = installment.recommended_message;
                opt.value = installment.installments;
                document.getElementById('installments').appendChild(opt);
            });
            
            $('.installments').val(response[0].payer_costs[0].installments);
        } else {
            alert(`installments method info error: ${response}`);
        }
    });
}

function doPay_(){ 

	var $button = document.querySelector('.mercadopago-button');
	
	stop__();

	$button.click();
	
	return false;

};

function doPay(){ 

	var $form = document.querySelector('#form');
	
	window.Mercadopago.createToken($form, sdkResponseHandler);
	
	return false;

};

function sdkResponseHandler(status, response) {
    if (status != 200 && status != 201) {
        alert("verify filled data");
        stop__();
    }else{
    	$('.token').val(response.id);
        
        submitForm([ {
			name : 'token',
			value : response.id
		} ]);
    }
};


function codigoEscaneado(barcode) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Código Escaneado',
		  text: "Código: " + barcode,
		  type: 'success',
		  showCancelButton: false,
		  confirmButtonText: 'OK',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  $('.btn-close-dlgbarcode').click();
			  $('.btn-barcode').click();
			  							  						  
			  reading = false;	
			  		 
		  }
		})	
}


function codigoEscaneado_(barcode) {
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Código Escaneado',
		  text: "Código: " + barcode,
		  type: 'success',
		  showCancelButton: false,
		  confirmButtonText: 'OK',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  $('.btn-close-dlgbarcode').click();
			  $('.process-barcode').click();							  

			  reading = false;
			  		 
		  }
		})	
}


function devolucaoRealizada() {
	
	let timerInterval;
	Swal.fire({
	  type: 'success',
	  title: 'Concluído!',
	  html: 'Devolução realizada com sucesso, redirecionando para fazer a troca.<br/><b></b>',
	  timer: 8000,
	  allowOutsideClick: false,
	  timerProgressBar: true,
	  onBeforeOpen: () => {
	    Swal.showLoading()
	    timerInterval = setInterval(() => {
	      
	    }, 100)
	  },
	  onClose: () => {
	    clearInterval(timerInterval)
	  }
	}).then((result) => {
	  /* Read more about handling dismissals below */
	  if (result.dismiss === Swal.DismissReason.timer) {
		  $(location).attr('href','/decore-webapp/PDV.xhtml');
	  }
	})
}


function trocarItens() {
	
	let timerInterval;
	Swal.fire({
	  type: 'success',
	  title: 'Aguarde!',
	  html: 'Redirecionando para fazer a troca.<br/><b></b>',
	  timer: 3000,
	  allowOutsideClick: false,
	  timerProgressBar: true,
	  onBeforeOpen: () => {
	    Swal.showLoading()
	    timerInterval = setInterval(() => {
	      
	    }, 100)
	  },
	  onClose: () => {
	    clearInterval(timerInterval)
	  }
	}).then((result) => {
	  /* Read more about handling dismissals below */
	  if (result.dismiss === Swal.DismissReason.timer) {
		  $(location).attr('href','PDV.xhtml');
	  }
	})
}

function fecharCaixa(valor) {
	
	var numero = valor.toFixed(2).split('.');
	numero[0] = "R$ " + numero[0].split(/(?=(?:...)*$)/).join('.');

	valor = numero.join(',');
	
	const swalWithBootstrapButtons = swal.mixin({
		  confirmButtonClass: 'btn btn-success',
		  cancelButtonClass: 'btn btn-danger',
		  buttonsStyling: false,
		})

		swalWithBootstrapButtons({
		  title: 'Fechar Caixa',
		  text: "O caixa será fechado em " + valor + ". Confirmar esse valor?",
		  type: 'warning',
		  showCancelButton: true,
		  confirmButtonText: 'Sim, confirmar!',
		  cancelButtonText: 'Não, cancelar!',
		  reverseButtons: true
		}).then((result) => {
		  if (result.value) {
			  
			  setarItemID();
			  		 
		  }
		})	
}

function abrirDialogLancamentoReceita() {
	setarItemID();
}

function abrirDialogLancamentoDespesa() {
	setarItemID_();
}

function atualizaSaldo(lancamento) {
	
	let timerInterval;
	Swal.fire({
	  type: 'success',
	  title: 'Concluído!',
	  html: 'Lançamento N.'
			+ lancamento + ' registrado com sucesso! <br/>Atualizando saldo.<br/><b></b>',
	  timer: 3000,
	  allowOutsideClick: false,
	  timerProgressBar: true,
	  onBeforeOpen: () => {
	    Swal.showLoading()
	    timerInterval = setInterval(() => {
	      
	    }, 100)
	  },
	  onClose: () => {
	    clearInterval(timerInterval)
	  }
	}).then((result) => {
	  /* Read more about handling dismissals below */
	  if (result.dismiss === Swal.DismissReason.timer) {
		  $(location).attr('href','Dashboard.xhtml');
	  }
	})
}

function downloadPDF() {
	$('.download-pdf').click();
}