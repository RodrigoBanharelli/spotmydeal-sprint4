-- =====================================================================
-- SoulUp / SpotMyDeal - Sprint 4
-- Script de criacao das 5 tabelas do MER
-- =====================================================================

-- Para recomecar do zero, rode os drops abaixo antes do script.
--  tabelas filhas saem primeiro, senao o Oracle recusa a exclusao por causa das chaves estrangeiras.
--
-- drop table t_smd_resgate         cascade constraints;
-- drop table t_smd_usuario_missao  cascade constraints;
-- drop table t_smd_missao          cascade constraints;
-- drop table t_smd_loja            cascade constraints;
-- drop table t_smd_usuario         cascade constraints;
-- drop sequence sq_t_smd_resgate;
-- drop sequence sq_t_smd_usuario_missao;
-- drop sequence sq_t_smd_missao;
-- drop sequence sq_t_smd_loja;
-- drop sequence sq_t_smd_usuario;


-- 1) USUARIO
create table t_smd_usuario (
    cd_usuario          number(9,0)   primary key,
    nm_usuario          varchar2(80)  not null,
    ds_email            varchar2(120) not null unique,
    vl_pontuacao        number(9,0)   default 0 not null,
    ds_nivel            varchar2(30)  default 'Iniciante' not null,
    nr_dias_ult_missao  number(4,0)   default 999 not null
);

create sequence sq_t_smd_usuario start with 1 increment by 1 nocache;


-- 2) LOJA
-- 1 = ativa, 0 = inativa. O LojaDao converte para o boolean do model.
create table t_smd_loja (
    cd_loja       number(9,0)  primary key,
    nm_loja       varchar2(80) not null,
    vl_latitude   number(9,6)  not null,
    vl_longitude  number(9,6)  not null,
    ds_cupom      varchar2(40) not null,
    st_ativa      number(1,0)  default 1 not null,
    constraint ck_smd_loja_ativa check (st_ativa in (0, 1))
);

create sequence sq_t_smd_loja start with 1 increment by 1 nocache;


-- 3) MISSAO 
--Nao tem coluna "concluida": isso e do par usuario x missao, na tabela 4.
create table t_smd_missao (
    cd_missao       number(9,0)   primary key,
    nm_titulo       varchar2(80)  not null,
    ds_missao       varchar2(255),
    vl_pontos       number(6,0)   not null,
    nr_dias_limite  number(4,0)   not null,
    constraint ck_smd_missao_pontos check (vl_pontos > 0)
);

create sequence sq_t_smd_missao start with 1 increment by 1 nocache;


-- 4) USUARIO_MISSAO - relacionamento N para N entre usuario e missao
-- A constraint UNIQUE garante no proprio banco que o mesmo usuario nao conclui a mesma missao duas vezes, mesmo que a regra da BO falhe.
create table t_smd_usuario_missao (
    cd_usuario_missao  number(9,0) primary key,
    cd_usuario         number(9,0) not null,
    cd_missao          number(9,0) not null,
    dt_conclusao       date        not null,
    vl_pontos_ganhos   number(6,0) not null,
    constraint fk_smd_um_usuario foreign key (cd_usuario) references t_smd_usuario (cd_usuario),
    constraint fk_smd_um_missao  foreign key (cd_missao)  references t_smd_missao (cd_missao),
    constraint uk_smd_um_par     unique (cd_usuario, cd_missao)
);

create sequence sq_t_smd_usuario_missao start with 1 increment by 1 nocache;


-- 5) RESGATE - relacionamento N para N entre usuario e loja
-- Aqui NAO ha unique: o mesmo usuario pode resgatar na mesma loja varias vezes, em datas diferentes. 
--O cupom e copiado no momento do resgate para o historico nao mudar se a loja trocar a oferta depois.
create table t_smd_resgate (
    cd_resgate        number(9,0)  primary key,
    cd_usuario        number(9,0)  not null,
    cd_loja           number(9,0)  not null,
    ds_cupom          varchar2(40) not null,
    dt_resgate        date         not null,
    vl_pontos_ganhos  number(6,0)  not null,
    constraint fk_smd_resgate_usuario foreign key (cd_usuario) references t_smd_usuario (cd_usuario),
    constraint fk_smd_resgate_loja    foreign key (cd_loja)    references t_smd_loja (cd_loja)
);

create sequence sq_t_smd_resgate start with 1 increment by 1 nocache;


-- =====================================================================
-- DADOS DE EXEMPLO - para ter o que testar na API 
-- =====================================================================

insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, vl_pontuacao, ds_nivel, nr_dias_ult_missao)
values (sq_t_smd_usuario.nextval, 'Rodrigo Banharelli', 'rodrigo@soulup.com.br', 520, 'Ecologista', 2);

insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, vl_pontuacao, ds_nivel, nr_dias_ult_missao)
values (sq_t_smd_usuario.nextval, 'Tayna Jimenes', 'tayna@soulup.com.br', 860, 'Sustentavel Avancado', 5);

insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, vl_pontuacao, ds_nivel, nr_dias_ult_missao)
values (sq_t_smd_usuario.nextval, 'Visitante Teste', 'teste@soulup.com.br', 0, 'Iniciante', 999);

-- Coordenadas reais da regiao da Paulista, para o calculo de distancia fazer sentido
insert into t_smd_loja (cd_loja, nm_loja, vl_latitude, vl_longitude, ds_cupom, st_ativa)
values (sq_t_smd_loja.nextval, 'Cafe Verde Paulista', -23.561414, -46.655881, 'CAFE20', 1);

insert into t_smd_loja (cd_loja, nm_loja, vl_latitude, vl_longitude, ds_cupom, st_ativa)
values (sq_t_smd_loja.nextval, 'Mercado Organico Consolacao', -23.557820, -46.661920, 'ORGANICO15', 1);

insert into t_smd_loja (cd_loja, nm_loja, vl_latitude, vl_longitude, ds_cupom, st_ativa)
values (sq_t_smd_loja.nextval, 'Bike Shop Augusta', -23.554300, -46.662500, 'BIKE10', 0);

insert into t_smd_missao (cd_missao, nm_titulo, ds_missao, vl_pontos, nr_dias_limite)
values (sq_t_smd_missao.nextval, 'Levar sacola retornavel', 'Fazer uma compra usando sacola reutilizavel', 150, 30);

insert into t_smd_missao (cd_missao, nm_titulo, ds_missao, vl_pontos, nr_dias_limite)
values (sq_t_smd_missao.nextval, 'Ir de bicicleta', 'Deslocar-se ate uma loja parceira de bicicleta', 200, 15);

insert into t_smd_missao (cd_missao, nm_titulo, ds_missao, vl_pontos, nr_dias_limite)
values (sq_t_smd_missao.nextval, 'Descarte correto de pilhas', 'Levar pilhas usadas a um ponto de coleta', 300, 60);

-- Conclusoes e resgates de exemplo, para os endpoints de historico ja virem com dados.
-- As chaves vem de subconsulta pelo e-mail / nome, e nao do numero 1, 2, 3: quem gera o
-- id e a sequence, e se o script rodar numa base que ja tenha dados os numeros mudam.
insert into t_smd_usuario_missao (cd_usuario_missao, cd_usuario, cd_missao, dt_conclusao, vl_pontos_ganhos)
values (sq_t_smd_usuario_missao.nextval,
        (select cd_usuario from t_smd_usuario where ds_email = 'rodrigo@soulup.com.br'),
        (select cd_missao  from t_smd_missao  where nm_titulo = 'Levar sacola retornavel'),
        sysdate - 2, 150);

insert into t_smd_usuario_missao (cd_usuario_missao, cd_usuario, cd_missao, dt_conclusao, vl_pontos_ganhos)
values (sq_t_smd_usuario_missao.nextval,
        (select cd_usuario from t_smd_usuario where ds_email = 'tayna@soulup.com.br'),
        (select cd_missao  from t_smd_missao  where nm_titulo = 'Levar sacola retornavel'),
        sysdate - 5, 150);

insert into t_smd_usuario_missao (cd_usuario_missao, cd_usuario, cd_missao, dt_conclusao, vl_pontos_ganhos)
values (sq_t_smd_usuario_missao.nextval,
        (select cd_usuario from t_smd_usuario where ds_email = 'tayna@soulup.com.br'),
        (select cd_missao  from t_smd_missao  where nm_titulo = 'Descarte correto de pilhas'),
        sysdate - 5, 300);

insert into t_smd_resgate (cd_resgate, cd_usuario, cd_loja, ds_cupom, dt_resgate, vl_pontos_ganhos)
values (sq_t_smd_resgate.nextval,
        (select cd_usuario from t_smd_usuario where ds_email = 'rodrigo@soulup.com.br'),
        (select cd_loja    from t_smd_loja    where nm_loja   = 'Cafe Verde Paulista'),
        'CAFE20', sysdate - 1, 122);

commit;




-- ---------------------------------------------------------------------
-- Ajuste do ranking, para a vitrine do front ter nomes e pontuacao de verdade.
--
-- A chave aqui e o e-mail, e nao o codigo. Quem gera o codigo e a sequence, e numa
-- base recem-criada ele nao e o mesmo numero de hoje.
--
-- O nivel vai junto com a pontuacao de proposito: ds_nivel e coluna no banco, e quem
-- recalcula ela no Java e o adicionarPontos(), que so roda quando o usuario conclui
-- missao. Mexendo na pontuacao por SQL, o nivel tem que vir na mesma linha - senao a
-- API devolve 2340 pontos com nivel "Iniciante", que e a faixa de quem tem menos de 500.
-- A regra esta no Usuario.atualizarNivel(): 500 Ecologista, 800 Sustentavel Avancado,
-- 1000 Lenda Verde.
-- ---------------------------------------------------------------------

update t_smd_usuario
   set nm_usuario = 'rodrigo', vl_pontuacao = 2340, ds_nivel = 'Lenda Verde'
 where ds_email = 'rodrigo@soulup.com.br';

update t_smd_usuario
   set nm_usuario = 'Tayna Jimenes', vl_pontuacao = 1890, ds_nivel = 'Lenda Verde'
 where ds_email = 'tayna@soulup.com.br';

-- Os outros dois integrantes nao existiam no script: entravam so pelo POST da API, e por
-- isso o update por codigo fixo (8 e 11) nao encontrava ninguem numa base zerada.
insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, vl_pontuacao, ds_nivel, nr_dias_ult_missao)
values (sq_t_smd_usuario.nextval, 'Gabriel A.', 'gabriel@email.com', 1540, 'Lenda Verde', 3);

insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, vl_pontuacao, ds_nivel, nr_dias_ult_missao)
values (sq_t_smd_usuario.nextval, 'Nycolas Melo', 'Melomelo@email.com', 1120, 'Lenda Verde', 4);

-- ---------------------------------------------------------------------
-- Rede de seguranca do nivel.
--
-- Esta instrucao recalcula ds_nivel para TODA a tabela, na mesma regra do
-- Usuario.atualizarNivel() do Java. Rode sempre que alterar vl_pontuacao na mao:
-- sem ela, a API devolve pontuacao alta com nivel de iniciante, porque o nivel
-- so e recalculado no Java quando o usuario conclui uma missao.
-- ---------------------------------------------------------------------

update t_smd_usuario
   set ds_nivel = case
         when vl_pontuacao >= 1000 then 'Lenda Verde'
         when vl_pontuacao >=  800 then 'Sustentavel Avancado'
         when vl_pontuacao >=  500 then 'Ecologista'
         else 'Iniciante'
       end;

commit;


-- CONFERENCIA
select * from t_smd_usuario;
select * from t_smd_loja;
select * from t_smd_missao;
select * from t_smd_usuario_missao;
select * from t_smd_resgate;

