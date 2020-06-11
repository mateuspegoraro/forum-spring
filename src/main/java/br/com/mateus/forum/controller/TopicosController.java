package br.com.mateus.forum.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.mateus.forum.controller.dto.DetalhesDoTopicoDto;
import br.com.mateus.forum.controller.dto.TopicoDto;
import br.com.mateus.forum.controller.dto.TopicoFormUpdate;
import br.com.mateus.forum.controller.form.TopicoForm;
import br.com.mateus.forum.model.Topico;
import br.com.mateus.forum.repository.CursoRepository;
import br.com.mateus.forum.repository.TopicoRepository;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@GetMapping
	@Cacheable(value = "listaDeTopicos")
	public Page<TopicoDto> lista(@RequestParam(required = false) String nomeCurso, 
			@PageableDefault(sort = "id", direction = Direction.DESC, page = 0, size = 10) Pageable paginacao){
		if(nomeCurso == null) {
			Page<Topico> topicos = this.topicoRepository.findAll(paginacao);
			return TopicoDto.converter(topicos);
		} else {
			Page<Topico> topicos = this.topicoRepository.findByCursoNome(nomeCurso, paginacao);
			return TopicoDto.converter(topicos);
		}
	}
	
	@PostMapping
	@Transactional
	@CacheEvict(value = {"listaDeTopicos"}, allEntries = true)
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {
		Topico topico = form.converter(this.cursoRepository);
		topicoRepository.save(topico);
		TopicoDto dto = new TopicoDto(topico);
		
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		
		return ResponseEntity.created(uri).body(dto);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DetalhesDoTopicoDto> detalhar(@PathVariable("id") Long codTopico) { // Ou @PathVariable Long id
		Optional<Topico> topico = this.topicoRepository.findById(codTopico);
		if(topico.isPresent()) {
			return ResponseEntity.ok(new DetalhesDoTopicoDto(topico.get()));
		} else {
			return ResponseEntity.notFound().build();
		}
		
	}
	
	@PutMapping("/{id}")
	@Transactional
	@CacheEvict(value = {"listaDeTopicos"}, allEntries = true)
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid TopicoFormUpdate form){
		Optional<Topico> optional = this.topicoRepository.findById(id);
		if(optional.isPresent()) {
			Topico topico = form.atualizar(id, this.topicoRepository);
			return ResponseEntity.ok(new TopicoDto(topico));
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	@CacheEvict(value = {"listaDeTopicos"}, allEntries = true)
	public ResponseEntity<?> remover(@PathVariable Long id) {
		Optional<Topico> optional = this.topicoRepository.findById(id);
		if(optional.isPresent()) {
			this.topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
		
	}
}
