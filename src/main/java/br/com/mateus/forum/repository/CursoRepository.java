package br.com.mateus.forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.mateus.forum.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long>{

	Curso findByNome(String nomeCurso);

}
